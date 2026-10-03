package spring.ai.example.spring_ai_demo.service;

/**
 * @author Tahereh Kasehpoor
 */

public interface AIService {

    String ask(
            String question,
            String conversationId
    );
}
