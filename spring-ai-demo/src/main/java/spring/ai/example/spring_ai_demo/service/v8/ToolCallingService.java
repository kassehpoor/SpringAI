
package spring.ai.example.spring_ai_demo.service.v8;



import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchResultDTO;
import spring.ai.example.spring_ai_demo.service.v7.ChunkSearchService;

import java.util.List;

@Slf4j
@Service
public class ToolCallingService {

    private static final String INSUFFICIENT_EVIDENCE =
            "اطلاعات کافی برای پاسخ دقیق به این سؤال "
                    + "در اسناد موجود نیست.";

    private final ChatClient chatClient;
    private final InsuranceKnowledgeTools insuranceKnowledgeTools;
    private final ChunkSearchService chunkSearchService;

    public ToolCallingService(
            ChatClient chatClient,
            InsuranceKnowledgeTools insuranceKnowledgeTools,
            ChunkSearchService chunkSearchService) {

        this.chatClient = chatClient;
        this.insuranceKnowledgeTools = insuranceKnowledgeTools;
        this.chunkSearchService = chunkSearchService;
    }

    public String ask(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be blank");
        }

        String normalizedQuestion = normalize(question);

        if (asksForMinimumRecord(normalizedQuestion)) {

            ChunkSearchRequestDTO request =
                    new ChunkSearchRequestDTO();

            request.setQuery(question.trim());
            request.setTopK(5);

            List<ChunkSearchResultDTO> results =
                    chunkSearchService.search(request);

            boolean hasExplicitEvidence =
                    results != null
                            && results.stream()
                            .filter(result -> result.getContent() != null)
                            .anyMatch(result -> {
                                String content =
                                        normalize(result.getContent());

                                return content.contains("حداقل سابقه")
                                        && (content.contains("سال")
                                        || content.contains("ماه"));
                            });

            log.info(
                    "Minimum-record evidence check completed: found={}",
                    hasExplicitEvidence);

            if (!hasExplicitEvidence) {
                log.warn(
                        "Insufficient evidence for minimum-record question");
                return INSUFFICIENT_EVIDENCE;
            }
        }

        try {
            String answer = chatClient.prompt()
                    .system("""
                            تو دستیار دانش بیمه و مستمری هستی.

                            تمام پاسخ را فقط به زبان فارسی بنویس.
                            فقط از اطلاعات صریحاً موجود در نتایج ابزار
                            استفاده کن.
                            هیچ قانون، عدد، شرط یا سابقه‌ای را حدس نزن.
                            اگر شواهد کافی نیست، اعلام کن اطلاعات کافی
                            وجود ندارد.
                            نام فایل منبع را ذکر کن.
                            پاسخ را کوتاه، دقیق و خوانا بنویس.
                            """)
                    .user(question.trim())
                    .tools(insuranceKnowledgeTools)
                    .call()
                    .content();

            if (answer == null || answer.isBlank()) {
                log.warn("Model returned an empty answer");
                return INSUFFICIENT_EVIDENCE;
            }

            log.info("Tool calling request completed successfully");
            return answer.trim();

        } catch (Exception exception) {
            log.error("Tool calling request failed", exception);
            throw exception;
        }
    }

    private boolean asksForMinimumRecord(String question) {
        return question.contains("حداقل سابقه")
                || question.contains("سابقه قانونی")
                || question.contains("سابقه لازم");
    }

    private String normalize(String text) {
        return text
                .replace('ي', 'ی')
                .replace('ك', 'ک')
                .replace('\u200c', ' ')
                .trim();
    }
}
