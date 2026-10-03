package spring.ai.example.spring_ai_demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tahereh Kasehpoor
 */



// http://localhost:8080/ask?question=Hello

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {

        try {

            return chatClient
                    .prompt(question)
                    .call()
                    .content();

        } catch (Exception ex) {

            return "ERROR : " + ex.getMessage();
        }
    }
}