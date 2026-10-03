package spring.ai.example.spring_ai_demo.config;

import org.springframework.ai.chat.client.ChatClient;
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
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

}