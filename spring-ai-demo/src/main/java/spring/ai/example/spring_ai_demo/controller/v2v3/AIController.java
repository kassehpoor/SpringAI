package spring.ai.example.spring_ai_demo.controller.v2v3;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.v2.AIRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v2.AIResponseDTO;
import spring.ai.example.spring_ai_demo.dto.v3.InsuranceAnalysisDTO;
import spring.ai.example.spring_ai_demo.service.v2v3.AIService;


/**
 * @author Tahereh Kasehpoor
 */





/**
 * http://localhost:9999/swagger-ui/index.html
 */


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2v3/ai")
public class AIController {

    private final AIService aiService;

    @PostMapping("/v3/analyze")
    public InsuranceAnalysisDTO analyze(
            @RequestBody AIRequestDTO request) {

        return aiService.analyzeInsuranceQuestion(request);
    }

    @PostMapping("/v2/chat")
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