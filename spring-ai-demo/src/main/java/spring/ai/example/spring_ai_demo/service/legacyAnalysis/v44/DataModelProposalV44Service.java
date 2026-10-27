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

    public DataModelProposalV44Service(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            RequirementAnalysisRepository requirementRepository,
            DataModelProposalV44Repository proposalRepository,
            OracleMetadataService oracleMetadataService,
            @Value("${spring.ai.ollama.chat.options.model:llama3.2}")
            String modelName, ProjectStructureIndexService projectIndexService) {

        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.requirementRepository = requirementRepository;
        this.proposalRepository = proposalRepository;
        this.oracleMetadataService = oracleMetadataService;
        this.modelName = modelName;
        this.projectIndexService = projectIndexService;
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

        List<Map<String, Object>> javaEvidence =
                collectJavaEvidence(selectedTables);

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

        String proposalJson = toJson(proposal);
        String tablesJson = toJson(selectedTables);

        Long proposalId = proposalRepository.save(
                request.analysisId(),
                schema,
                tablesJson,
                proposalJson,
                proposal.mermaidErd()
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
            List<String> selectedTables) {

        var index = projectIndexService.getCurrentIndex();

        if (index == null || index.classes() == null) {
            return List.of();
        }

        List<Map<String, Object>> evidence = new ArrayList<>();
        Set<String> names = selectedTables.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        for (ClassInfo classInfo : index.classes().values()) {
            if (evidence.size() >= 100) {
                break;
            }

            Path file = index.sourceDirectory()
                    .resolve(classInfo.path())
                    .normalize();

            if (!file.startsWith(index.sourceDirectory().normalize())
                    || !Files.isRegularFile(file)) {
                continue;
            }

            try {
                List<String> lines = Files.readAllLines(
                        file, StandardCharsets.UTF_8);

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);

                    boolean matched = names.stream()
                            .anyMatch(name -> line.toUpperCase(Locale.ROOT)
                                    .contains(name));

                    if (!matched) {
                        continue;
                    }

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("className", classInfo.qualifiedName());
                    item.put("file", classInfo.path());
                    item.put("line", i + 1);
                    item.put("source", line.trim());
                    item.put("evidenceType", "JAVA_TEXT_MATCH");
                    item.put("confidence", "LOW");
                    item.put(
                            "warning",
                            "تطبیق متنی به‌تنهایی اثبات‌کننده ارتباط اجرایی نیست."
                    );

                    evidence.add(item);

                    if (evidence.size() >= 100) {
                        break;
                    }
                }
            } catch (IOException ex) {
                // فایل غیرقابل‌خواندن نباید کل تولید مدل را متوقف کند.
            }
        }

        return evidence;
    }

}