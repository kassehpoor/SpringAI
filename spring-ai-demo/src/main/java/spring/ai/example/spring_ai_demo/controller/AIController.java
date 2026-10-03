package spring.ai.example.spring_ai_demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.AIRequestDTO;
import spring.ai.example.spring_ai_demo.dto.AIResponseDTO;
import spring.ai.example.spring_ai_demo.service.AIService;

/**
 * http://localhost:9999/swagger-ui/index.html
 */


/**
 * @author Tahereh Kasehpoor
 */

/**
 *
 * Conversation
 *       ↓
 * Conversation ID
 *       ↓
 * ChatMemory
 *       ↓
 * Memory Repository
 *       ↓
 * Memory Advisor
 *       ↓
 * ChatClient
 *       ↓
 * LLM
 *
 *
 *
 *
 *
 *
 *                     Memory
 *                        │
 *                        ▼
 * User → Spring AI → Context → Ollama → LLM
 *
 *
 * ****************************************************
 *
 * Spring Boot JVM
 *       │
 *       ▼
 * Memory
 *       │
 *       ▼
 * RAM
 *
 * ****************************************************
 *
 * Application Start
 *        ↓
 * Memory = empty
 *
 */

// http://localhost:8080/ask?conversationId=test-1&question=My%20name%20is%20Tahereh

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public AIResponseDTO chat(
            @RequestBody AIRequestDTO request
    ) {

        return aiService.ask(request);
    }
}


/**
 *
 *
 * مثلاً در RAG:
 *
 * PDF
 *  ↓
 * Chunk
 *  ↓
 * Embedding
 *  ↓
 * Vector DB
 *  ↓
 * Similarity Search
 *  ↓
 * Context
 *  ↓
 * LLM
 *
 *
 *
 *
 *
 *
 *اما Memory:
 *
 * Conversation
 *  ↓
 * ChatMemory
 *  ↓
 * Previous messages
 *  ↓
 * LLM
 *
 *
 *
 */