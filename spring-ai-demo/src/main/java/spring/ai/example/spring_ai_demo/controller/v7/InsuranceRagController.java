package spring.ai.example.spring_ai_demo.controller.v7;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagResponseDTO;
import spring.ai.example.spring_ai_demo.service.v7.InsuranceRagService;

/**
 * @author Tahereh Kasehpoor
 */


/**+
 Question
 ↓
 nomic-embed-text
 ↓
 768-dimensional vector
 ↓
 pgvector cosine distance
 ↓
 Top K
 ↓
 LLM
 */



/**
 {
 "question": "شرایط بازنشستگی بیمه شده چیست؟",
 "topK": 3
 }
 */

/**
 {
 "question": "چه کسانی بعد از فوت بیمه شده می‌توانند مستمری دریافت کنند؟",
 "topK": 3
 }

 04-survivors-pension.pdf
 */

@RestController
@RequestMapping("/api/v7/rag")
@RequiredArgsConstructor
public class InsuranceRagController {

    private final InsuranceRagService ragService;

    @PostMapping
    public InsuranceRagResponseDTO ask(
            @Valid @RequestBody InsuranceRagRequestDTO request) {

        return ragService.ask(request);
    }
}


/**
 پس V6 و V7 را در ذهن این‌طور جدا کن
 V6 — Retrieval
 Question
 ↓
 Embedding
 ↓
 pgvector
 ↓
 Top K Documents

 سؤال:

 کدام اسناد به سؤال من شبیه‌ترند؟

 V7 — Retrieval + Generation
 Question
 ↓
 Embedding
 ↓
 pgvector
 ↓
 Top K Documents
 ↓
 Context
 ↓
 LLM
 ↓
 Answer

 سؤال:

 با استفاده از اسناد مرتبط، پاسخ سؤال من چیست؟

 این همان R در RAG = Retrieval و G در RAG = Generation است.
 */


/**
 ┌─────────────────┐
 │     Question    │
 └────────┬────────┘
 ↓
 ┌─────────────────┐
 │    Embedding    │
 │ nomic-embed-text│
 └────────┬────────┘
 ↓
 ┌─────────────────┐
 │    pgvector     │
 │ cosine distance │
 └────────┬────────┘
 ↓
 Top K results
 ↓
 ┌─────────────────┐
 │     Context     │
 └────────┬────────┘
 ↓
 ┌─────────────────┐
 │    llama3.2     │
 │   Generation    │
 └────────┬────────┘
 ↓
 Answer
 */