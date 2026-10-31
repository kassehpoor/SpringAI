package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v44;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

            metadata.add(Map.of(
                    "tableName", table,
                    "columns", columns,
                    "constraints",
                    oracleMetadataService.listConstraints(schema, table)
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
                 {
                   "title": "Pension Service",
                   "designRationale": "Design rationale",
                   "entities": [],
                   "relationships": [],
                   "businessConstraints": [
                     "Constraint 1",
                     "Constraint 2"
                   ],
                   "unresolvedQuestions": [
                     "Question 1"
                   ]
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
                - این شواهد صرفاً تطبیق متنی هستند و اثبات قطعی ارتباط نیستند.
                - از آن‌ها برای شناخت احتمالی منطق تجاری و کاربرد جدول‌ها استفاده کن.
                - موارد بدون شواهد کافی را در unresolvedQuestions ثبت کن.
                - نام کلاس یا متد را بدون وجود شواهد معتبر، قطعی اعلام نکن.
                
                یک دیتامدل جدید مناسب Spring Boot طراحی کن.
                """.formatted(
                modelName,
                analysisJson,
                metadataJson,
                javaEvidenceJson
        );

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

                if (linkResult.matches() == null) {
                    continue;
                }

                for (var match : linkResult.matches()) {
                    if (evidence.size() >= 200) {
                        break;
                    }

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("oracleObject", table);
                    item.put("className", match.className());
                    item.put("methodName", match.methodName());
                    item.put("file", match.path());
                    item.put("line", match.line());
                    item.put("source", match.evidence());
                    item.put("evidenceType", "V25_JAVA_ORACLE_TEXT_LINK");
                    item.put("confidence", "LOW");
                    item.put(
                            "warning",
                            "تطبیق متنی نام شیء با سورس Java است؛ " +
                                    "ارتباط اجرایی یا نگاشت قطعی اثبات نشده است."
                    );

                    evidence.add(item);
                }

            } catch (IOException | IllegalArgumentException ex) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("oracleObject", table);
                item.put("evidenceType", "V25_ANALYSIS_ERROR");
                item.put("confidence", "NONE");
                item.put("message", ex.getMessage());

                evidence.add(item);
            }
        }

        return evidence;
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