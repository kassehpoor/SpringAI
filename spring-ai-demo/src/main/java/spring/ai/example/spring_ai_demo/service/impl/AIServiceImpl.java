package spring.ai.example.spring_ai_demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.service.AIService;

/**
 * @author Tahereh Kasehpoor
 */


@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final ChatClient chatClient;

    @Override
    public String ask(
            String question,
            String conversationId
    ) {

        return chatClient
                .prompt()
                .user(question)
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,
                        conversationId
                ))
                .call()
                .content();
    }
}
