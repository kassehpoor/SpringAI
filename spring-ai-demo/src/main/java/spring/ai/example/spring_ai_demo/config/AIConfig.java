package spring.ai.example.spring_ai_demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Tahereh Kasehpoor
 */

/**
 *
 *
 * دلیلش این است که ChatClient.create(...) روی abstraction خود Spring AI کار می‌کند و باید ChatModel عمومی Spring AI را Inject کنی، نه مدل اختصاصی OpenAI.
 */
@Configuration
public class AIConfig {

    @Bean
    public ChatMemory chatMemory() {

        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient chatClient(
            ChatModel chatModel,
            ChatMemory chatMemory
    ) {
/**
 *
 * یعنی:
 *
 * هر request این ChatClient از این Memory Advisor استفاده کند.
 *
 * بنابراین بعداً در Service لازم نیست هر بار advisor را بسازیم.
 */
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .build()
                )
                .build();
    }
}