package spring.ai.example.spring_ai_demo.service.v8;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
public class ToolCallingService {

    private final ChatClient chatClient;
    private final InsuranceKnowledgeTools insuranceKnowledgeTools;

    public ToolCallingService(
            ChatClient chatClient,
            InsuranceKnowledgeTools insuranceKnowledgeTools) {
        this.chatClient = chatClient;
        this.insuranceKnowledgeTools = insuranceKnowledgeTools;
    }

    public String ask(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be blank");
        }

        return chatClient.prompt()
                .system("""
                    تو دستیار هوشمند حوزه بیمه و مستمری هستی.

                    برای پرسش‌های مرتبط با مقررات و اسناد بیمه،
                    از ابزار جست‌وجوی دانش استفاده کن.

                    فقط بر اساس نتایج ابزار پاسخ بده.
                    اگر شواهد کافی نیست، صریحاً اعلام کن.
                    نام فایل منبع را در پاسخ ذکر کن.
                    هیچ قانون یا عدد بیمه‌ای را حدس نزن.
                    پاسخ را به زبان فارسی ارائه بده.
                    """)
                .user(question.trim())
                .tools(insuranceKnowledgeTools)
                .call()
                .content();
    }
}
