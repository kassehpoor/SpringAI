package spring.ai.example.spring_ai_demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.AIRequestDTO;
import spring.ai.example.spring_ai_demo.dto.AIResponseDTO;
import spring.ai.example.spring_ai_demo.service.AIService;

/**
 * @author Tahereh Kasehpoor
 */


@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final ChatClient chatClient;

    @Override
    public AIResponseDTO ask(AIRequestDTO request) {

        String answer = chatClient
                .prompt()
                .system("""
                            You are an insurance AI assistant.
                        
                            Rules:
                            - Answer professionally.
                            - Prefer Persian language.
                            - Use the conversation context when relevant.
                        """)
                .user(request.getQuestion())
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,
                        request.getConversationId()
                ))
                .call()
                .content();

        return AIResponseDTO.builder()
                .answer(answer)
                .build();
    }
}

