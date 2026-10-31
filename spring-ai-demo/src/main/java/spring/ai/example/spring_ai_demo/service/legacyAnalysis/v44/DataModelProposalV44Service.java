package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v44;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v44.DataModelProposalV44DTO;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v44.DataModelProposalV44Request;
import spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v38.RequirementAnalysisRepository;
import spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v44.DataModelProposalV44Repository;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;
import spring.ai.example.spring_ai_demo.service.v25.JavaOracleLinkV25Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataModelProposalV44Service {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final RequirementAnalysisRepository requirementRepository;
    private final DataModelProposalV44Repository proposalRepository;
    private final OracleMetadataService oracleMetadataService;
    private final String modelName;
    private final ProjectStructureIndexService projectIndexService;
    private final JavaOracleLinkV25Service javaOracleLinkV25Service;

    public DataModelProposalV44Service(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            RequirementAnalysisRepository requirementRepository,
            DataModelProposalV44Repository proposalRepository,
            OracleMetadataService oracleMetadataService,
            @Value("${spring.ai.ollama.chat.options.model:llama3.2}")
            String modelName, ProjectStructureIndexService projectIndexService, JavaOracleLinkV25Service javaOracleLinkV25Service) {

        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.requirementRepository = requirementRepository;
        this.proposalRepository = proposalRepository;
        this.oracleMetadataService = oracleMetadataService;
        this.modelName = modelName;
        this.projectIndexService = projectIndexService;
        this.javaOracleLinkV25Service = javaOracleLinkV25Service;
    }

    @Transactional
    public Map<String, Object> generate(
            DataModelProposalV44Request request) {

        if (request == null || request.analysisId() == null
                || request.analysisId() <= 0) {
            throw new IllegalArgumentException(
                    "analysisId معتبر الزامی است.");
        }

        if (request.schema() == null || request.schema().isBlank()) {
            throw new IllegalArgumentException("Schema الزامی است.");
        }

        if (request.tableNames() == null
                || request.tableNames().isEmpty()) {
            throw new IllegalArgumentException(
                    "حداقل یک جدول مرتبط را در tableNames مشخص کن.");
        }

        if (request.tableNames().size() > 30) {
            throw new IllegalArgumentException(
                    "در هر درخواست حداکثر 30 جدول مجاز است.");
        }

        String schema = request.schema().trim()
                .toUpperCase(Locale.ROOT);

        // تحلیل ذخیره‌شده V38
        String analysisJson =
                requirementRepository.findAnalysisJsonById(
                        request.analysisId());

        if (analysisJson == null || analysisJson.isBlank()) {
            throw new IllegalArgumentException(
                    "تحلیل نیازمندی پیدا نشد.");
        }

        // استخراج فقط متادیتای جدول‌های انتخاب‌شده؛ نه کل Schema
        List<Map<String, Object>> metadata = new ArrayList<>();
        List<String> selectedTables = request.tableNames().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .map(name -> name.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();

        for (String table : selectedTables) {
            List<Map<String, Object>> columns =
                    oracleMetadataService.listColumns(schema, table);

            if (columns.isEmpty()) {
                throw new IllegalArgumentException(
                        "جدول در Schema پیدا نشد یا قابل مشاهده نیست: "
                                + table);
            }

            List<Map<String, Object>> constraints =
                    oracleMetadataService.listConstraints(schema, table);

// استخراج ستون‌های Primary Key فعال از Oracle
            Set<String> primaryKeyColumns = constraints.stream()
                    .filter(c -> "P".equalsIgnoreCase(value(c, "CONSTRAINT_TYPE")))
                    .filter(c -> "ENABLED".equalsIgnoreCase(value(c, "STATUS")))
                    .map(c -> value(c, "COLUMN_NAME"))
                    .filter(name -> !name.isBlank())
                    .collect(Collectors.toSet());

// نرمال‌سازی اطلاعات ستون‌ها
            List<Map<String, Object>> normalizedColumns = columns.stream()
                    .map(column -> {
                        Map<String, Object> normalized = new LinkedHashMap<>(column);

                        String nullable = value(column, "NULLABLE");
                        String columnName = value(column, "COLUMN_NAME");

                        normalized.put(
                                "nullable",
                                "Y".equalsIgnoreCase(nullable)
                        );

                        normalized.put(
                                "primaryKey",
                                primaryKeyColumns.contains(columnName)
                        );

                        return normalized;
                    })
                    .toList();

            metadata.add(Map.of(
                    "tableName", table,
                    "columns", normalizedColumns,
                    "constraints", constraints
            ));
        }

        Set<String> selectedSet = selectedTables.stream()
                .collect(Collectors.toSet());

        List<Map<String, Object>> foreignKeys =
                oracleMetadataService.listForeignKeys(schema).stream()
                        .filter(fk -> selectedSet.contains(
                                value(fk, "TABLE_NAME")))
                        .filter(fk -> selectedSet.contains(
                                value(fk, "REFERENCED_TABLE")))
                        .toList();

        String metadataJson = toJson(Map.of(
                "tables", metadata,
                "foreignKeys", foreignKeys
        ));

        // V44 برای هر جدول انتخاب‌شده از تحلیل موجود V25 استفاده کند و شواهد را همراه با نام کلاس و متد به مدل زبانی بدهد.
        List<Map<String, Object>> javaEvidence =
                collectJavaEvidence(schema, selectedTables);

        String javaEvidenceJson = toJson(javaEvidence);

        String systemPrompt = """
                تو معمار ارشد نرم‌افزار و تحلیل‌گر دیتامدل سامانه‌های
                سازمان تأمین اجتماعی هستی.
                
                بر اساس دو ورودی زیر، دیتامدل پیشنهادی جدید طراحی کن:
                1. تحلیل نیازمندی کسب‌وکار
                2. متادیتای واقعی Oracle
                
                قوانین:
                - ساختار قدیمی را کورکورانه کپی نکن.
                - نام ستون، قانون یا رابطه‌ای را که شواهدی برایش نداری
                  واقعیت قطعی اعلام نکن.
                - status برابر EVIDENCE_BASED فقط برای موردی است که
                  مستقیماً از شواهد ورودی پشتیبانی می‌شود.
                - status برابر PROPOSED برای طراحی جدید یا استنباطی است.
                - اگر نوع داده پیشنهادی است، آن را با شواهد موجود
                  توجیه کن و در sourceEvidence این محدودیت را ذکر کن.
                - کلید اصلی و خارجی را بدون شواهد قطعی، موجود فرض نکن؛
                  در صورت نیاز پیشنهاد بده.
                - اگر اطلاعات کافی نیست، unresolvedQuestions را پر کن.
                - روابط فقط بین موجودیت‌هایی باشند که در خروجی تعریف شده‌اند.
                - mermaidErd باید Mermaid erDiagram معتبر باشد.
                - توضیحات و نام‌های نمایشی را تا حد امکان فارسی بنویس؛
                  شناسه‌های Mermaid و نام فنی فیلدها انگلیسی باشند.
                - خروجی فقط مطابق DTO درخواستی باشد، بدون متن اضافی.
                - هرگز از example.com، URL ساختگی یا شواهد فرضی استفاده نکن.
                - sourceEvidence باید به شواهد واقعی ورودی اشاره کند؛
                  مانند نام جدول، نام ستون، constraint، مسیر فایل و شماره خط.
                - اگر برای یک ویژگی شاهد نداری، صریحاً بنویس:
                  "بدون شاهد مستقیم؛ پیشنهاد طراحی".
                - موجودیتی مانند Employee را صرفاً به دلیل متداول بودن
                  در سیستم‌های بازنشستگی اضافه نکن.
                - هر دو سر هر رابطه باید در entities تعریف شده باشند.
                - برای هر موجودیت، شناسه Mermaid یکتا بساز.
                - mermaidErd باید با entities و relationships هماهنگ باشد.
                - برای جدول Oracle، نام واقعی جدول و نام واقعی ستون‌ها را
                  در شواهد نگه دار؛ نام‌گذاری جدید را از نام Oracle جدا کن.
                - فیلد mermaidErd باید فقط کد خام Mermaid از نوع erDiagram باشد.
                  - داخل mermaidErd از علامت ``` یا Markdown code fence استفاده نکن.
                  - از ساختار class استفاده نکن؛ فقط از ساختار erDiagram استفاده کن.
                  - خروجی باید JSON معتبر و مطابق DTO باشد.
                  - رشته‌های JSON نباید حاوی newline واقعی و escape‌نشده باشند.
                  - برای newline داخل رشته JSON از \\n استفاده کن.
                  - فیلد mermaidErd باید فقط کد خام Mermaid از نوع erDiagram باشد.
                  - داخل mermaidErd از علامت ``` یا Markdown code fence استفاده نکن.
                  - از ساختار class استفاده نکن؛ فقط از ساختار erDiagram استفاده کن.
                  - خروجی باید JSON معتبر و مطابق DTO باشد.
                  - رشته‌های JSON نباید حاوی newline واقعی و escape‌نشده باشند.
                  - برای newline داخل رشته JSON از \\n استفاده کن.
                 قوانین دقیق ساختار JSON:
                 - businessConstraints حتماً باید آرایه‌ای از String باشد.
                 - unresolvedQuestions حتماً باید آرایه‌ای از String باشد.
                 - entities حتماً باید آرایه‌ای از EntityProposal باشد.
                 - relationships حتماً باید آرایه‌ای از RelationshipProposal باشد.
                 - هر AttributeProposal باید دقیقاً مطابق فیلدهای DTO تولید شود.
                 - برای businessConstraints هرگز object شامل name و description تولید نکن.
                 - برای هر محدودیت تجاری، یک رشته مستقل تولید کن.
                 - خروجی باید دقیقاً با ساختار DataModelProposalV44DTO منطبق باشد.
                 قوانین اجباری نوع داده در JSON:
                
                 - nullable باید فقط مقدار boolean واقعی JSON باشد:
                   true یا false.
                 - primaryKey باید فقط مقدار boolean واقعی JSON باشد:
                   true یا false.
                 - هرگز برای این دو فیلد از "Y"، "N"، "YES"، "NO" یا رشته استفاده نکن.
                 - مقدار nullable از ستون NULLABLE در متادیتای Oracle استخراج شود:
                   Y یعنی true و N یعنی false.
                 - مقدار primaryKey فقط زمانی true است که ستون در constraint
                   با نوع P و وضعیت ENABLED حضور داشته باشد.
                 - constraintهای نوع U را Primary Key محسوب نکن.
                 - برای هر AttributeProposal، sourceEvidence باید شامل نام جدول،
                   نام ستون و اطلاعات نوع داده Oracle باشد.
                 - sourceEvidence مربوط به EntityProposal نباید خالی باشد.
                 - اگر اطلاعات کافی برای ایجاد یک رابطه وجود ندارد،
                   relationships باید آرایه خالی باشد؛ رابطه ناقص نساز.
                 - اگر اطلاعات کافی برای یک موجودیت وجود ندارد،
                   آن را به‌صورت حدس قطعی تولید نکن.
                 - status فقط یکی از مقادیر EVIDENCE_BASED یا PROPOSED باشد.
                 JSON OUTPUT RULES:
                
                 1. Return valid JSON matching the DataModelProposalV44DTO structure exactly.
                
                 2. For every AttributeProposal:
                    - nullable must be a JSON boolean: true or false.
                    - primaryKey must be a JSON boolean: true or false.
                    - Never use "Y", "N", "YES", "NO", "true", or "false" as strings.
                
                 3. Oracle NULLABLE mapping:
                    - Y means nullable = true.
                    - N means nullable = false.
                
                 4. Primary key identification:
                    - primaryKey = true only when the column belongs to an ENABLED
                      Oracle primary key constraint (constraint type P).
                    - A UNIQUE constraint (type U) is not a primary key.
                    - Disabled constraints must not be treated as active primary keys.
                    - If no enabled primary key exists for a table, its attributes
                      must not be marked as primary keys.
                
                 5. Evidence:
                    - Every entity must have non-empty sourceEvidence.
                    - Every attribute must have non-empty sourceEvidence.
                    - Attribute evidence must identify the Oracle table and column.
                    - Java JPA mapping evidence may support entity mapping,
                      but must not be used as column-level evidence.
                    - Never invent file paths, line numbers, table names, or evidence.
                
                 6. Relationships:
                    - Create a relationship only when supported by explicit foreign-key
                      metadata or reliable Java relationship mapping evidence.
                    - A shared column name alone does not prove a relationship.
                    - If no relationship is supported by evidence, return [].
                    - Never return relationships with empty sourceEntity or targetEntity.
                
                 7. Status:
                    - Use only EVIDENCE_BASED or PROPOSED.
                    - EVIDENCE_BASED means directly supported by supplied evidence.
                    - PROPOSED means a design suggestion that is not directly confirmed.
                    - Do not present a proposal as an existing database fact.
                
                 8. Do not create generic entities such as "دیتامدل جدید".
                    Entity names must be meaningful and grounded in the supplied evidence.
                
                 9. unresolvedQuestions must contain meaningful questions only.
                    If there are no unresolved questions, return [].
                    Never return empty strings.
                 {
                   "title": "Proposed Data Model",
                   "designRationale": "Design rationale",
                   "entities": [
                     {
                       "name": "Request",
                       "description": "درخواست",
                       "attributes": [
                         {
                           "name": "requestNumber",
                           "dataType": "String",
                           "nullable": false,
                           "primaryKey": true,
                           "description": "شماره درخواست",
                           "sourceEvidence": "LSU_REQ.REQ_NOREQ - Oracle CHAR(13)",
                           "status": "EVIDENCE_BASED"
                         }
                       ],
                       "sourceEvidence": "Oracle table LSU_REQ; Java class Request",
                       "status": "EVIDENCE_BASED"
                     }
                   ],
                   "relationships": [],
                   "businessConstraints": [],
                   "unresolvedQuestions": []
                 }
                """;

        String userPrompt = """
                مدل مورد استفاده: %s
                
                تحلیل نیازمندی ذخیره‌شده:
                %s
                
                متادیتای واقعی Oracle برای جدول‌های انتخاب‌شده:
                %s
                شواهد استخراج‌شده از سورس Java پروژه قدیمی:
                %s
                
                نحوه استفاده از شواهد:
                - شواهد دارای evidenceType برابر JPA_TABLE_MAPPING
                  نشان‌دهنده تطبیق دقیق نام جدول در annotation مربوط به JPA هستند.
                - این شواهد فقط نگاشت کلاس Java به جدول Oracle را پشتیبانی می‌کنند.
                - از وجود @Table به‌تنهایی، رابطه بین دو موجودیت را نتیجه‌گیری نکن.
                - برای ایجاد رابطه، به Foreign Key یا شواهد معتبر دیگری نیاز داری.
                - اگر برای یک جدول نگاشت JPA پیدا نشده است، آن را در
                  unresolvedQuestions ثبت کن.
                - هیچ کلاس یا جدولی را بر اساس شباهت بخشی از نام‌ها مرتبط نکن.
                
                یک دیتامدل جدید مناسب Spring Boot طراحی کن.
                """.formatted(
                modelName,
                analysisJson,
                metadataJson,
                javaEvidenceJson
        );

        log.info("========== V44 INPUT DEBUG ==========");
        log.info("ANALYSIS JSON:\n{}", analysisJson);
        log.info("ORACLE METADATA:\n{}", metadataJson);
        log.info("JAVA EVIDENCE:\n{}", javaEvidenceJson);
        log.info("========== END V44 INPUT DEBUG ==========");

        DataModelProposalV44DTO proposal = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(DataModelProposalV44DTO.class);

        if (proposal == null || proposal.entities() == null
                || proposal.entities().isEmpty()) {
            throw new IllegalStateException(
                    "مدل هوش مصنوعی دیتامدل معتبری تولید نکرد.");
        }

        validateProposal(proposal);

        String proposalJson = toJson(proposal);
        String tablesJson = toJson(selectedTables);

        Long proposalId = proposalRepository.save(
                request.analysisId(),
                schema,
                tablesJson,
                proposalJson,
""
//                proposal.mermaidErd()
        );

        return Map.of(
                "proposalId", proposalId,
                "analysisId", request.analysisId(),
                "schema", schema,
                "tableCountUsed", selectedTables.size(),
                "status", "DRAFT",
                "proposal", proposal
        );
    }

    public Map<String, Object> getProposal(Long id) {
        return proposalRepository.findById(id);
    }

    public List<Map<String, Object>> listByAnalysis(Long analysisId) {
        return proposalRepository.findByAnalysisId(analysisId);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "تبدیل خروجی به JSON ناموفق بود.", e);
        }
    }

    private String value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toLowerCase(Locale.ROOT));
        }
        return value == null ? "" : value.toString()
                .toUpperCase(Locale.ROOT);
    }


    private List<Map<String, Object>> collectJavaEvidence(
            String schema,
            List<String> selectedTables) {

        List<Map<String, Object>> evidence = new ArrayList<>();

        if (selectedTables == null || selectedTables.isEmpty()) {
            return evidence;
        }

        for (String table : selectedTables) {

            if (evidence.size() >= 200) {
                break;
            }

            try {
                var linkResult =
                        javaOracleLinkV25Service.analyze(schema, table);

                if (linkResult == null || linkResult.matches() == null) {
                    continue;
                }

                for (var match : linkResult.matches()) {

                    if (evidence.size() >= 200) {
                        break;
                    }

                    String source = match.evidence();

                    // فقط نگاشت مستقیم JPA را قبول می‌کنیم.
                    if (!hasExactJpaTableMapping(source, table)) {
                        continue;
                    }

                    Map<String, Object> item = new LinkedHashMap<>();

                    item.put("oracleObject", table);
                    item.put("className", match.className());
                    item.put("methodName", match.methodName());
                    item.put("file", match.path());
                    item.put("line", match.line());
                    item.put("source", source);

                    item.put("evidenceType", "JPA_TABLE_MAPPING");
                    item.put("confidence", "HIGH");

                    item.put(
                            "warning",
                            "نام جدول در annotation مربوط به JPA به‌صورت دقیق "
                                    + "با جدول انتخاب‌شده مطابقت دارد."
                    );

                    evidence.add(item);
                }

            } catch (IOException | IllegalArgumentException ex) {

                log.warn(
                        "V44: دریافت شواهد Java برای جدول {} ناموفق بود: {}",
                        table,
                        ex.getMessage()
                );
            }
        }

        return evidence;
    }

    private boolean hasExactJpaTableMapping(
            String source,
            String expectedTable) {

        if (source == null || source.isBlank()
                || expectedTable == null || expectedTable.isBlank()) {
            return false;
        }

        // پیدا کردن annotation از نوع @Table(...)
        java.util.regex.Pattern tableAnnotationPattern =
                java.util.regex.Pattern.compile(
                        "@Table\\s*\\(([^)]*)\\)",
                        java.util.regex.Pattern.CASE_INSENSITIVE
                );

        java.util.regex.Matcher annotationMatcher =
                tableAnnotationPattern.matcher(source);

        while (annotationMatcher.find()) {

            String annotationBody = annotationMatcher.group(1);

            // استخراج مقدار name یا value از annotation
            java.util.regex.Pattern namePattern =
                    java.util.regex.Pattern.compile(
                            "\\b(?:name|value)\\s*=\\s*[\"']([^\"']+)[\"']",
                            java.util.regex.Pattern.CASE_INSENSITIVE
                    );

            java.util.regex.Matcher nameMatcher =
                    namePattern.matcher(annotationBody);

            while (nameMatcher.find()) {

                String actualTable = nameMatcher.group(1)
                        .trim()
                        .toUpperCase(Locale.ROOT);

                if (actualTable.equals(
                        expectedTable.trim().toUpperCase(Locale.ROOT))) {
                    return true;
                }
            }
        }

        return false;
    }

    private void validateProposal(DataModelProposalV44DTO proposal) {
        Set<String> entityNames = new HashSet<>();

        for (var entity : proposal.entities()) {
            if (entity == null || entity.name() == null
                    || entity.name().isBlank()) {
                throw new IllegalStateException(
                        "V44: نام موجودیت خالی است.");
            }

            if (!entityNames.add(
                    entity.name().trim().toLowerCase(Locale.ROOT))) {
                throw new IllegalStateException(
                        "V44: نام موجودیت تکراری است: " + entity.name());
            }

            rejectFakeEvidence(entity.sourceEvidence());

            if (entity.attributes() != null) {
                for (var attribute : entity.attributes()) {
                    if (attribute == null || attribute.name() == null
                            || attribute.name().isBlank()) {
                        throw new IllegalStateException(
                                "V44: ویژگی بدون نام در موجودیت "
                                        + entity.name());
                    }

                    rejectFakeEvidence(attribute.sourceEvidence());
                }
            }
        }

        if (proposal.relationships() != null) {
            for (var relationship : proposal.relationships()) {
                if (relationship == null
                        || relationship.sourceEntity() == null
                        || relationship.targetEntity() == null) {
                    throw new IllegalStateException(
                            "V44: رابطه ناقص است.");
                }

                if (!entityNames.contains(
                        relationship.sourceEntity().trim()
                                .toLowerCase(Locale.ROOT))
                        || !entityNames.contains(
                        relationship.targetEntity().trim()
                                .toLowerCase(Locale.ROOT))) {
                    throw new IllegalStateException(
                            "V44: رابطه به موجودیت تعریف‌نشده اشاره می‌کند: "
                                    + relationship.sourceEntity() + " -> "
                                    + relationship.targetEntity());
                }

                rejectFakeEvidence(relationship.sourceEvidence());
            }
        }

//        if (proposal.mermaidErd() == null
//                || !proposal.mermaidErd().contains("erDiagram")) {
//            throw new IllegalStateException(
//                    "V44: نمودار Mermaid خالی یا نامعتبر است.");
//        }
    }

    private void rejectFakeEvidence(String evidence) {
        if (evidence == null || evidence.isBlank()) {
            throw new IllegalStateException(
                    "V44: شواهد منبع خالی است. مقدار دریافتی: ["
                            + evidence + "]");
        }

        String normalized = evidence.toLowerCase(Locale.ROOT);

        if (normalized.contains("example.com")
                || normalized.contains("lorem ipsum")
                || normalized.contains("placeholder")) {
            throw new IllegalStateException(
                    "V44: شواهد ساختگی یا placeholder شناسایی شد: "
                            + evidence);
        }
    }


}