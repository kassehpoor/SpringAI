package spring.ai.example.spring_ai_demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.AIRequestDTO;
import spring.ai.example.spring_ai_demo.dto.AIResponseDTO;
import spring.ai.example.spring_ai_demo.dto.InsuranceAnalysisDTO;
import spring.ai.example.spring_ai_demo.service.AIService;


/**
 * @author Tahereh Kasehpoor
 */





/**
 * http://localhost:9999/swagger-ui/index.html
 */


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AIController {

    private final AIService aiService;

    @PostMapping("/analyze")
    public InsuranceAnalysisDTO analyze(
            @RequestBody AIRequestDTO request) {

        return aiService.analyzeInsuranceQuestion(request);
    }

    @PostMapping("/chat")
    public AIResponseDTO chat(
            @RequestBody AIRequestDTO request
    ) {

        return aiService.ask(request);
    }
}


/**
 *
 *                  conversationId
 *                        │
 *                        ▼
 *                   ┌─────────┐
 *                   │ Memory  │
 *                   └────┬────┘
 *                        │
 *                  previous messages
 *                        │
 *                        ▼
 * User ────────> ChatClient
 *                    │
 *                    ▼
 *               Memory Advisor
 *                    │
 *                    ▼
 *              Previous Context
 *                    +
 *              Current Question
 *                    │
 *                    ▼
 *                  Ollama
 *                    │
 *                    ▼
 *                 llama3.2
 *                    │
 *                    ▼
 *                 Answer
 *                    │
 *                    ▼
 *                 Memory
 *
 *
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