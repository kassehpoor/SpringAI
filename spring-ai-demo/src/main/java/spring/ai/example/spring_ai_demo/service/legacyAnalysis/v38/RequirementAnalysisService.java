
package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v38;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v38.RequirementAnalysisDTO;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v38.RequirementAnalysisRequest;
import spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v38.RequirementAnalysisRepository;


@Service
public class RequirementAnalysisService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final RequirementAnalysisRepository repository;
    private final String modelName;

    public RequirementAnalysisService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            RequirementAnalysisRepository repository,
            @Value("${spring.ai.ollama.chat.options.model:llama3.2}")
            String modelName) {

        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.modelName = modelName;
    }

    @Transactional
    public SavedRequirementAnalysis analyze(RequirementAnalysisRequest request) {

        String systemPrompt = """
            تو دستیار تحلیل سامانه مستمری سازمان تأمین اجتماعی ایران هستی.
            متن ورودی ممکن است فارسی و شامل قواعد کسب‌وکار باشد.

            فقط بر اساس متن ارائه‌شده تحلیل کن.
            هیچ قانون، جدول، ویژگی، رابطه یا محدودیت جدیدی را به‌عنوان واقعیت اختراع نکن.
            اگر اطلاعات کافی نیست، موضوع را در ambiguities قرار بده.
            موارد پیشنهادی را از واقعیت‌های صریح سند متمایز کن.
            برای هر نیازمندی و قاعده، sourceReference را فقط در صورت وجود
            ارجاع قابل اتکا در ورودی پر کن؛ در غیر این صورت null بگذار.
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
                request.documentName(),
                request.sourceReference() == null
                        ? "ارائه نشده"
                        : request.sourceReference(),
                request.requirementText()
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
                request.documentName(),
                "DRAFT",
                analysis.requirements() == null
                        ? java.util.List.of() : analysis.requirements(),
                analysis.businessRules() == null
                        ? java.util.List.of() : analysis.businessRules(),
                analysis.entities() == null
                        ? java.util.List.of() : analysis.entities(),
                analysis.relationships() == null
                        ? java.util.List.of() : analysis.relationships(),
                analysis.constraints() == null
                        ? java.util.List.of() : analysis.constraints(),
                analysis.ambiguities() == null
                        ? java.util.List.of() : analysis.ambiguities()
        );

        try {
            String json = objectMapper.writeValueAsString(draft);

            Long id = repository.saveDraft(
                    request.documentName(),
                    request.sourceReference(),
                    request.requirementText(),
                    json,
                    modelName
            );

            return new SavedRequirementAnalysis(id, draft, json);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "تبدیل تحلیل به JSON ناموفق بود.", exception
            );
        }
    }

    public record SavedRequirementAnalysis(
            Long id,
            RequirementAnalysisDTO analysis,
            String analysisJson
    ) {}
}
