
package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v38;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v38.RequirementAnalysisDTO;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v38.RequirementAnalysisRequest;
import spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v38.RequirementAnalysisRepository;
import spring.ai.example.spring_ai_demo.service.v4.PdfDocumentService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RequirementAnalysisService {

    private static final int CHUNK_SIZE = 10_000;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final RequirementAnalysisRepository repository;
    private final PdfDocumentService pdfDocumentService;
    private final String modelName;

    public RequirementAnalysisService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            RequirementAnalysisRepository repository,
            PdfDocumentService pdfDocumentService,
            @Value("${spring.ai.ollama.chat.options.model:llama3.2}")
            String modelName) {

        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.pdfDocumentService = pdfDocumentService;
        this.modelName = modelName;
    }

    /**
     * تحلیل متن ارسالی از API فعلی V38
     */
    @Transactional
    public SavedRequirementAnalysis analyze(
            RequirementAnalysisRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "درخواست تحلیل الزامی است."
            );
        }

        return analyzeAndSave(
                request.documentName(),
                request.sourceReference(),
                request.requirementText()
        );
    }

    /**
     * تحلیل مستقیم PDF با پردازش قطعه‌ای
     */
    @Transactional
    public SavedRequirementAnalysis analyzePdf(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "فایل PDF خالی است."
            );
        }

        Document document = pdfDocumentService.createDocument(file);

        String text = document.getText();

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "متنی از PDF استخراج نشد؛ برای PDF اسکن‌شده OCR لازم است."
            );
        }

        Object metadataFileName =
                document.getMetadata().get("fileName");

        String fileName =
                metadataFileName instanceof String name && !name.isBlank()
                        ? name
                        : "document.pdf";

        return analyzeAndSave(
                fileName,
                "PDF: " + fileName,
                text
        );
    }

    /**
     * متن را قطعه‌بندی، تحلیل، تجمیع و یک‌بار ذخیره می‌کند.
     */
    private SavedRequirementAnalysis analyzeAndSave(
            String documentName,
            String sourceReference,
            String text) {

        if (documentName == null || documentName.isBlank()) {
            throw new IllegalArgumentException(
                    "نام سند الزامی است."
            );
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "متن برای تحلیل خالی است."
            );
        }

        List<String> chunks = splitText(text);

        List<RequirementAnalysisDTO.RequirementItem> requirements =
                new ArrayList<>();

        List<RequirementAnalysisDTO.BusinessRuleItem> businessRules =
                new ArrayList<>();

        List<RequirementAnalysisDTO.EntityItem> entities =
                new ArrayList<>();

        List<RequirementAnalysisDTO.RelationshipItem> relationships =
                new ArrayList<>();

        List<String> constraints = new ArrayList<>();
        List<String> ambiguities = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {

            String chunkReference = sourceReference
                    + " | بخش " + (i + 1)
                    + " از " + chunks.size();

            RequirementAnalysisDTO result = analyzeChunk(
                    documentName,
                    chunkReference,
                    chunks.get(i)
            );

            requirements.addAll(
                    result.requirements() == null
                            ? List.of() : result.requirements()
            );

            businessRules.addAll(
                    result.businessRules() == null
                            ? List.of() : result.businessRules()
            );

            entities.addAll(
                    result.entities() == null
                            ? List.of() : result.entities()
            );

            relationships.addAll(
                    result.relationships() == null
                            ? List.of() : result.relationships()
            );

            if (result.constraints() != null) {
                constraints.addAll(result.constraints());
            }

            if (result.ambiguities() != null) {
                ambiguities.addAll(result.ambiguities());
            }
        }

        RequirementAnalysisDTO draft = new RequirementAnalysisDTO(
                documentName,
                "DRAFT",
                validRequirements(requirements),
                validBusinessRules(businessRules),
                validEntities(entities),
                validRelationships(relationships),
                uniqueNonBlank(constraints),
                uniqueNonBlank(ambiguities)
        );

        if (!hasMeaningfulContent(draft)) {
            throw new IllegalStateException(
                    "مدل هیچ نتیجه معتبری استخراج نکرد. "
                            + "رکورد خالی ذخیره نشد؛ متن PDF و خروجی مدل را بررسی کن."
            );
        }

        try {
            String json = objectMapper.writeValueAsString(draft);

            Long id = repository.saveDraft(
                    documentName,
                    sourceReference,
                    text,
                    json,
                    modelName
            );

            return new SavedRequirementAnalysis(
                    id,
                    draft,
                    json
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "تبدیل تحلیل به JSON ناموفق بود.",
                    exception
            );
        }
    }

    /**
     * تحلیل یک قطعه متن توسط مدل
     */
    private RequirementAnalysisDTO analyzeChunk(
            String documentName,
            String sourceReference,
            String chunk) {

        String systemPrompt = """
                تو دستیار تحلیل نیازمندی‌های سامانه مستمری
                سازمان تأمین اجتماعی ایران هستی.

                فقط بر اساس متن همین بخش تحلیل کن.
                اطلاعاتی را که در متن وجود ندارد اختراع نکن.
                هر مورد باید شواهد متنی قابل تشخیص داشته باشد.
                اگر نام یا توضیح یک مورد مشخص نیست، آن مورد را نساز.
                موجودیت‌ها و روابط پیشنهادی را از واقعیت‌های صریح جدا کن.
                برای اطلاعات ناکافی از ambiguities استفاده کن.
                رشته خالی را برای نام موجودیت یا توضیح قاعده تولید نکن.
                اگر دسته‌ای موردی ندارد، آرایه آن را خالی برگردان.
                status باید DRAFT باشد.
                خروجی باید مطابق ساختار RequirementAnalysisDTO باشد.
                """;

        String userPrompt = """
                نام سند: %s
                ارجاع این بخش: %s

                متن بخش:
                %s
                """.formatted(
                documentName,
                sourceReference,
                chunk
        );

        RequirementAnalysisDTO result = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(RequirementAnalysisDTO.class);

        if (result == null) {
            throw new IllegalStateException(
                    "مدل برای یکی از بخش‌های سند خروجی نداد: "
                            + sourceReference
            );
        }

        return result;
    }

    /**
     * تقسیم متن با ترجیح مرز پاراگراف
     */
    private List<String> splitText(String text) {

        List<String> chunks = new ArrayList<>();
        String remaining = text.strip();

        while (!remaining.isEmpty()) {

            if (remaining.length() <= CHUNK_SIZE) {
                chunks.add(remaining);
                break;
            }

            int splitAt = remaining.lastIndexOf(
                    "\n",
                    CHUNK_SIZE
            );

            if (splitAt < CHUNK_SIZE / 2) {
                splitAt = CHUNK_SIZE;
            }

            String chunk = remaining.substring(0, splitAt).strip();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            remaining = remaining.substring(splitAt).strip();
        }

        return chunks;
    }

    private List<RequirementAnalysisDTO.RequirementItem>
    validRequirements(
            List<RequirementAnalysisDTO.RequirementItem> items) {

        return items.stream()
                .filter(item -> item != null)
                .filter(item -> hasText(item.title())
                        || hasText(item.description()))
                .collect(
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toCollection(
                                        java.util.LinkedHashSet::new
                                ),
                                ArrayList::new
                        )
                );
    }

    private List<RequirementAnalysisDTO.BusinessRuleItem>
    validBusinessRules(
            List<RequirementAnalysisDTO.BusinessRuleItem> items) {

        return items.stream()
                .filter(item -> item != null)
                .filter(item -> hasText(item.description()))
                .collect(
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toCollection(
                                        java.util.LinkedHashSet::new
                                ),
                                ArrayList::new
                        )
                );
    }

    private List<RequirementAnalysisDTO.EntityItem> validEntities(
            List<RequirementAnalysisDTO.EntityItem> items) {

        return items.stream()
                .filter(item -> item != null)
                .filter(item -> hasText(item.name()))
                .map(item -> new RequirementAnalysisDTO.EntityItem(
                        item.name().trim(),
                        uniqueNonBlank(item.attributes() == null
                                ? List.of() : item.attributes()),
                        item.sourceReference()
                ))
                .collect(
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toCollection(
                                        java.util.LinkedHashSet::new
                                ),
                                ArrayList::new
                        )
                );
    }

    private List<RequirementAnalysisDTO.RelationshipItem>
    validRelationships(
            List<RequirementAnalysisDTO.RelationshipItem> items) {

        return items.stream()
                .filter(item -> item != null)
                .filter(item -> hasText(item.sourceEntity())
                        && hasText(item.targetEntity()))
                .collect(
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toCollection(
                                        java.util.LinkedHashSet::new
                                ),
                                ArrayList::new
                        )
                );
    }

    private List<String> uniqueNonBlank(List<String> values) {

        Set<String> unique = new LinkedHashSet<>();

        for (String value : values) {
            if (hasText(value)) {
                unique.add(value.trim());
            }
        }

        return new ArrayList<>(unique);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasMeaningfulContent(
            RequirementAnalysisDTO result) {

        return !result.requirements().isEmpty()
                || !result.businessRules().isEmpty()
                || !result.entities().isEmpty()
                || !result.relationships().isEmpty()
                || !result.constraints().isEmpty()
                || !result.ambiguities().isEmpty();
    }

    public record SavedRequirementAnalysis(
            Long id,
            RequirementAnalysisDTO analysis,
            String analysisJson) {
    }
}
