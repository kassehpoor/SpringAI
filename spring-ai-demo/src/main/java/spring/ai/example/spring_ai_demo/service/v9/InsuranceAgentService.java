package spring.ai.example.spring_ai_demo.service.v9;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchResultDTO;
import spring.ai.example.spring_ai_demo.service.v7.ChunkSearchService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tahereh Kasehpoor
 */

//این نسخه از همان ChunkSearchService مرحله V7 استفاده می‌کند؛ بنابراین لازم نیست جست‌وجوی pgvector را دوباره پیاده‌سازی کنیم.

@Service
public class InsuranceAgentService {

    private final ChatClient chatClient;
    private final ChunkSearchService chunkSearchService;

    public InsuranceAgentService(
            ChatClient chatClient,
            ChunkSearchService chunkSearchService) {
        this.chatClient = chatClient;
        this.chunkSearchService = chunkSearchService;
    }

    public String ask(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be blank");
        }

        ChunkSearchRequestDTO request =
                new ChunkSearchRequestDTO();

        request.setQuery(question.trim());
        request.setTopK(5);

        List<ChunkSearchResultDTO> results =
                chunkSearchService.search(request);

        if (results == null || results.isEmpty()) {
            return "اطلاعات مرتبطی در اسناد موجود پیدا نشد.";
        }

        String context = results.stream()
                .map(result -> """
                        فایل: %s
                        صفحه: %s
                        محتوا: %s
                        """.formatted(
                        result.getFileName(),
                        result.getPageNumber(),
                        result.getContent()))
                .collect(Collectors.joining("\n---\n"));

        return chatClient.prompt()
                .system("""
                        تو دستیار مهندسی دانش بیمه و مستمری هستی.

                        فقط بر اساس متن منابع ارائه‌شده پاسخ بده.
                        قوانین، اعداد و شرایطی را که در منابع نیست
                        حدس نزن.
                        اگر منابع برای پاسخ کافی نیستند،
                        این محدودیت را صریح اعلام کن.
                        نام فایل منبع را ذکر کن.
                        پاسخ را به زبان فارسی بده.
                        """)
                .user("""
                        پرسش:
                        %s

                        منابع بازیابی‌شده:
                        %s
                        """.formatted(question.trim(), context))
                .call()
                .content();
    }
}

