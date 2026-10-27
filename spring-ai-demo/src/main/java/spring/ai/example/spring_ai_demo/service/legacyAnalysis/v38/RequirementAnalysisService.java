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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RequirementAnalysisService {

    private static final int CHUNK_SIZE = 10_000;
    private static final long MAX_TEXT_FILE_SIZE = 2L * 1024 * 1024;

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
     * تحلیل متن ارسالی از API فعلی V38.
     */
    @Transactional
    public SavedRequirementAnalysis analyze(RequirementAnalysisRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("درخواست تحلیل الزامی است.");
        }

        return analyzeAndSave(
                request.documentName(),
                request.sourceReference(),
                request.requirementText()
        );
    }

    /**
     * تحلیل مستقیم PDF با پردازش قطعه‌ای.
     */
    @Transactional
    public SavedRequirementAnalysis analyzePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("فایل PDF خالی است.");
        }

        Document document = pdfDocumentService.createDocument(file);
        String text = document.getText();

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "متنی از PDF استخراج نشد؛ برای PDF اسکن‌شده OCR لازم است."
            );
        }

        Object metadataFileName = document.getMetadata().get("fileName");
        String fileName = metadataFileName instanceof String name && !name.isBlank()
                ? name
                : "document.pdf";

        return analyzeAndSave(
                fileName,
                "PDF: " + fileName,
                text
        );
    }

    /**
     * متن را قطعه‌بندی، تحلیل، تجمیع، اعتبارسنجی و یک‌بار ذخیره می‌کند.
     */
    private SavedRequirementAnalysis analyzeAndSave(
            String documentName,
            String sourceReference,
            String text) {

        if (documentName == null || documentName.isBlank()) {
            throw new IllegalArgumentException("نام سند الزامی است.");
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("متن برای تحلیل خالی است.");
        }

        List<String> chunks = splitText(text);

        List<RequirementAnalysisDTO.RequirementItem> requirements = new ArrayList<>();
        List<RequirementAnalysisDTO.BusinessRuleItem> businessRules = new ArrayList<>();
        List<RequirementAnalysisDTO.EntityItem> entities = new ArrayList<>();
        List<RequirementAnalysisDTO.RelationshipItem> relationships = new ArrayList<>();
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
                    result.requirements() == null ? List.of() : result.requirements()
            );
            businessRules.addAll(
                    result.businessRules() == null ? List.of() : result.businessRules()
            );
            entities.addAll(
                    result.entities() == null ? List.of() : result.entities()
            );
            relationships.addAll(
                    result.relationships() == null ? List.of() : result.relationships()
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
                validTextValues(constraints),
                validTextValues(ambiguities)
        );

        if (!hasMeaningfulContent(draft)) {
            throw new IllegalStateException(
                    "مدل هیچ نتیجه معتبر و قابل استفاده‌ای استخراج نکرد. "
                            + "رکورد ذخیره نشد؛ متن استخراج‌شده و خروجی مدل را بررسی کن."
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

            return new SavedRequirementAnalysis(id, draft, json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "تبدیل تحلیل به JSON ناموفق بود.",
                    exception
            );
        }
    }

    /**
     * تحلیل یک قطعه متن توسط مدل.
     */
    private RequirementAnalysisDTO analyzeChunk(
            String documentName,
            String sourceReference,
            String chunk) {

        String systemPrompt = """
                تو دستیار تحلیل نیازمندی‌های سامانه‌های سازمان تأمین اجتماعی ایران هستی.

                فقط بر اساس متن بخش ارائه‌شده تحلیل کن.
                هیچ اطلاعات، نیازمندی، قانون کسب‌وکار، موجودیت، ویژگی یا رابطه‌ای را اختراع نکن.
                هر مورد خروجی باید مستقیماً از متن ورودی پشتیبانی شود.
                اگر شواهد کافی وجود ندارد، آن مورد را نساز.

                قوانین الزامی:
                1. از داده‌های نمونه (placeholder) استفاده نکن.
                2. عبارت‌هایی مانند Entity Name، Attribute 1، Requirement Title،
                   Requirement 1، Business Rule 1، Constraint 1، Entity Ref،
                   Req-1-Ref، BR-1-Ref و Relationship 1 داده واقعی نیستند.
                3. برای دسته‌ای که اطلاعات واقعی ندارد، آرایه خالی برگردان.
                4. ویژگی‌های هر موجودیت باید از متن استخراج شوند؛ نام موجودیت را
                   به‌عنوان ویژگی تکرار نکن، مگر آنکه متن صریحاً آن را تأیید کند.
                5. روابط را فقط زمانی ثبت کن که متن ارتباط بین دو موجودیت را پشتیبانی کند.
                6. شناسه‌ها را برای موارد استخراج‌شده تولید کن؛ شناسه به‌تنهایی
                   جای عنوان، توضیح یا شواهد واقعی را نمی‌گیرد.
                7. محدودیت‌ها فقط باید از متن استخراج شوند، نه از مثال یا دانش حدسی.
                8. متن فارسی را تا حد ممکن با همان معنای متن اصلی حفظ کن.
                9. اگر متن برای استخراج کافی نیست، آرایه‌های مربوط را خالی بگذار.
                10. status باید DRAFT باشد.
                11. sourceReference باید به ارجاع ارائه‌شده در درخواست اشاره کند.
                12. کلید روابط دقیقاً relationships است.

                خروجی باید با ساختار RequirementAnalysisDTO سازگار باشد.
                فقط JSON مطابق ساختار DTO برگردان و هیچ متن توضیحی خارج از JSON اضافه نکن.
                """;

        String userPrompt = """
                نام سند: %s
                ارجاع این بخش: %s

                متن بخش:
                %s
                """.formatted(documentName, sourceReference, chunk);

        RequirementAnalysisDTO result = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(RequirementAnalysisDTO.class);

        if (result == null) {
            throw new IllegalStateException(
                    "مدل برای یکی از بخش‌های سند خروجی نداد: " + sourceReference
            );
        }

        return result;
    }

    /**
     * تقسیم متن با ترجیح مرز پاراگراف.
     */
    private List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();
        String remaining = text.strip();

        while (!remaining.isEmpty()) {
            if (remaining.length() <= CHUNK_SIZE) {
                chunks.add(remaining);
                break;
            }

            int splitAt = remaining.lastIndexOf("\n", CHUNK_SIZE);

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

    private List<RequirementAnalysisDTO.RequirementItem> validRequirements(
            List<RequirementAnalysisDTO.RequirementItem> items) {

        if (items == null) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item != null)
                .filter(item -> hasRealText(item.title())
                        || hasRealText(item.description()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));
    }

    private List<RequirementAnalysisDTO.BusinessRuleItem> validBusinessRules(
            List<RequirementAnalysisDTO.BusinessRuleItem> items) {

        if (items == null) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item != null)
                .filter(item -> hasRealText(item.description()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));
    }

    private List<RequirementAnalysisDTO.EntityItem> validEntities(
            List<RequirementAnalysisDTO.EntityItem> items) {

        if (items == null) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item != null)
                .filter(item -> hasRealText(item.name()))
                .map(item -> new RequirementAnalysisDTO.EntityItem(
                        item.name().trim(),
                        validTextValues(item.attributes()),
                        item.sourceReference()
                ))
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));
    }

    private List<RequirementAnalysisDTO.RelationshipItem> validRelationships(
            List<RequirementAnalysisDTO.RelationshipItem> items) {

        if (items == null) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item != null)
                .filter(item -> hasRealText(item.sourceEntity())
                        && hasRealText(item.targetEntity())
                        && hasRealText(item.relationshipType()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));
    }

    private List<String> uniqueNonBlank(List<String> values) {
        if (values == null) {
            return List.of();
        }

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

    /**
     * شناسایی placeholderهای شناخته‌شده؛ این متد جایگزین بررسی معنایی کامل نیست.
     */
    private boolean isPlaceholder(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String normalized = value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s_-]+", " ");

        return normalized.matches(
                "entity name|attribute \\d+|requirement title|"
                        + "requirement \\d+|business rule \\d+|"
                        + "constraint \\d+|entity ref|req \\d+ ref|"
                        + "relationship \\d+|relationship \\d+ ref|"
                        + "br \\d+ ref|se \\d+|te \\d+|rt \\d+"
        );
    }

    private boolean hasRealText(String value) {
        return hasText(value) && !isPlaceholder(value);
    }

    private List<String> validTextValues(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(this::hasRealText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private boolean hasMeaningfulContent(RequirementAnalysisDTO result) {
        return result != null
                && ((!result.requirements().isEmpty())
                || (!result.businessRules().isEmpty())
                || (!result.entities().isEmpty())
                || (!result.relationships().isEmpty())
                || (!result.constraints().isEmpty())
                || (!result.ambiguities().isEmpty()));
    }

    public record SavedRequirementAnalysis(
            Long id,
            RequirementAnalysisDTO analysis,
            String analysisJson) {
    }

    /**
     * تحلیل فایل متنی UTF-8 با پسوند txt.
     */
    @Transactional
    public SavedRequirementAnalysis analyzeTextFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("فایل متنی خالی است.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null
                || !fileName.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            throw new IllegalArgumentException(
                    "فقط فایل با پسوند txt پذیرفته می‌شود."
            );
        }

        if (file.getSize() > MAX_TEXT_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "حداکثر حجم فایل متنی 2 مگابایت است."
            );
        }

        final String text;
        try {
            text = new String(file.getBytes(), StandardCharsets.UTF_8)
                    .replaceFirst("^\\uFEFF", "")
                    .strip();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "خواندن فایل متنی ناموفق بود.",
                    exception
            );
        }

        if (text.isBlank()) {
            throw new IllegalArgumentException("فایل فاقد متن قابل تحلیل است.");
        }

        return analyzeAndSave(
                fileName,
                "TEXT: " + fileName,
                text
        );
    }
}
