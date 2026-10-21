
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

import java.util.List;

@Service
public class RequirementAnalysisService {

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
     * تحلیل متن نیازمندی؛ سازگار با endpoint فعلی V38
     */
    @Transactional
    public SavedRequirementAnalysis analyze(
            RequirementAnalysisRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "درخواست تحلیل نمی‌تواند null باشد."
            );
        }

        return analyzeText(
                request.documentName(),
                request.sourceReference(),
                request.requirementText()
        );
    }

    /**
     * استخراج متن PDF و ارسال آن برای تحلیل V38
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

        String extractedText = document.getText();

        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalArgumentException(
                    "متنی از PDF استخراج نشد. "
                            + "اگر فایل اسکن‌شده است، ممکن است OCR لازم باشد."
            );
        }

        Object fileNameMetadata =
                document.getMetadata().get("fileName");

        String fileName = fileNameMetadata instanceof String name
                && !name.isBlank()
                ? name
                : "document.pdf";

        String sourceReference = "PDF: " + fileName;

        return analyzeText(
                fileName,
                sourceReference,
                extractedText
        );
    }

    /**
     * منطق مشترک تحلیل و ذخیره نتیجه
     */
    private SavedRequirementAnalysis analyzeText(
            String documentName,
            String sourceReference,
            String requirementText) {

        if (documentName == null || documentName.isBlank()) {
            throw new IllegalArgumentException(
                    "نام سند الزامی است."
            );
        }

        if (requirementText == null || requirementText.isBlank()) {
            throw new IllegalArgumentException(
                    "متن نیازمندی خالی است."
            );
        }

        String systemPrompt = """
                تو دستیار تحلیل سامانه مستمری سازمان تأمین اجتماعی ایران هستی.
                متن ورودی ممکن است فارسی و شامل قواعد کسب‌وکار باشد.

                فقط بر اساس متن ارائه‌شده تحلیل کن.
                هیچ قانون، جدول، ویژگی، رابطه یا محدودیت جدیدی را
                به‌عنوان واقعیت اختراع نکن.
                اگر اطلاعات کافی نیست، موضوع را در ambiguities قرار بده.
                موارد پیشنهادی را از واقعیت‌های صریح سند متمایز کن.
                برای هر نیازمندی و قاعده، sourceReference را فقط در صورت
                وجود ارجاع قابل اتکا در ورودی پر کن؛ در غیر این صورت null بگذار.
                موجودیت‌ها و روابط را به‌عنوان پیشنهاد اولیه در نظر بگیر.
                status را DRAFT قرار بده.
                پاسخ را مطابق ساختار DTO در قالب JSON قابل تبدیل تولید کن.
                """;

        String userPrompt = """
                نام سند: %s
                ارجاع منبع: %s

                متن نیازمندی:
                %s
                """.formatted(
                documentName,
                sourceReference == null
                        ? "ارائه نشده"
                        : sourceReference,
                requirementText
        );

        RequirementAnalysisDTO analysis = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(RequirementAnalysisDTO.class);

        if (analysis == null) {
            throw new IllegalStateException(
                    "مدل هوش مصنوعی خروجی ساختاریافته تولید نکرد."
            );
        }

        RequirementAnalysisDTO draft = new RequirementAnalysisDTO(
                documentName,
                "DRAFT",
                analysis.requirements() == null
                        ? List.of() : analysis.requirements(),
                analysis.businessRules() == null
                        ? List.of() : analysis.businessRules(),
                analysis.entities() == null
                        ? List.of() : analysis.entities(),
                analysis.relationships() == null
                        ? List.of() : analysis.relationships(),
                analysis.constraints() == null
                        ? List.of() : analysis.constraints(),
                analysis.ambiguities() == null
                        ? List.of() : analysis.ambiguities()
        );

        try {
            String json = objectMapper.writeValueAsString(draft);

            Long id = repository.saveDraft(
                    documentName,
                    sourceReference,
                    requirementText,
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

    public record SavedRequirementAnalysis(
            Long id,
            RequirementAnalysisDTO analysis,
            String analysisJson
    ) {
    }
}
