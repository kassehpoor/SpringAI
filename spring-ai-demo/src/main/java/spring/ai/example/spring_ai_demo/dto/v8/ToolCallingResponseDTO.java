package spring.ai.example.spring_ai_demo.dto.v8;

/**
 * @author Tahereh Kasehpoor
 */


public record ToolCallingResponseDTO(
        String question,
        String answer
) {
}