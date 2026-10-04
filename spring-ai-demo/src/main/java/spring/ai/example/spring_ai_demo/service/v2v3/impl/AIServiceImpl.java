package spring.ai.example.spring_ai_demo.service.v2v3.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v2.AIRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v2.AIResponseDTO;
import spring.ai.example.spring_ai_demo.dto.v3.InsuranceAnalysisDTO;
import spring.ai.example.spring_ai_demo.service.v2v3.AIService;

/**
 * @author Tahereh Kasehpoor
 */


@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final ChatClient chatClient;

    @Override
    public InsuranceAnalysisDTO analyzeInsuranceQuestion(AIRequestDTO request) {

        return chatClient
                .prompt()
                .system("""
                     You are an AI assistant.
                            Rules:
                            - Answer professionally.
                            - Prefer Persian language.
                            - Use the conversation context when relevant.
                    - Return a concise and professional analysis.
                    - The title field must contain a short title.
                    - The summary field must contain the main explanation.
                    - The category field must identify the topic.
                    - The requiredDocuments field must contain a list
                      of relevant documents.
                    - The requiresExpertReview field must be true
                      when an expert should review the case.
                    - Do not invent insurance laws or claim that
                      a legal condition is verified when it is not.
                    - If the question does not provide enough
                      information, state this in the summary.
                    """)
                .user(request.getQuestion())
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,
                        request.getConversationId()
                ))
                .call()
                .entity(InsuranceAnalysisDTO.class);
    }

    @Override
    public AIResponseDTO ask(AIRequestDTO request) {

        String answer = chatClient
                .prompt()
                .system("""
                            You are an AI assistant.
                        
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

