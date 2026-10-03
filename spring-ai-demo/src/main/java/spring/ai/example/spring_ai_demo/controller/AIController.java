package spring.ai.example.spring_ai_demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.service.AIService;

/**
 * @author Tahereh Kasehpoor
 */


// http://localhost:8080/ask?conversationId=test-1&question=My%20name%20is%20Tahereh

@RestController
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @GetMapping("/ask")
    public String ask(
            @RequestParam String question,
            @RequestParam String conversationId
    ) {

        try {

            return aiService.ask(
                    question,
                    conversationId
            );

        } catch (Exception ex) {

            return "ERROR : " + ex.getMessage();
        }
    }
}
