package spring.ai.example.spring_ai_demo.service.v7.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchResultDTO;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagResponseDTO;
import spring.ai.example.spring_ai_demo.service.v7.InsuranceRagService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tahereh Kasehpoor
 */
@Service
@RequiredArgsConstructor
public class InsuranceRagServiceImpl
        implements InsuranceRagService {

    private final EmbeddingModel embeddingModel;

    private final JdbcTemplate jdbcTemplate;

    private final ChatClient chatClient;

    @Override
    public InsuranceRagResponseDTO ask(
            InsuranceRagRequestDTO request) {

        String question =
                request.getQuestion().trim();

        // 1. Create embedding for user question
        float[] queryEmbedding =
                embeddingModel.embed(question);

        if (queryEmbedding.length != 768) {
            throw new IllegalStateException(
                    "Expected 768 dimensions, but received "
                            + queryEmbedding.length
            );
        }

        String vectorText =
                Arrays.toString(queryEmbedding);

        // 2. Search relevant documents
        String sql = """
                SELECT
                    id,
                    content,
                    embedding <=> CAST(? AS vector) AS distance
                FROM insurance_document
                ORDER BY embedding <=> CAST(? AS vector)
                LIMIT ?
                """;

        List<InsuranceDocumentSearchResultDTO> results =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                InsuranceDocumentSearchResultDTO.builder()
                                        .id(rs.getLong("id"))
                                        .content(rs.getString("content"))
                                        .distance(rs.getDouble("distance"))
                                        .build(),
                        vectorText,
                        vectorText,
                        request.getTopK()
                );

        // 3. Build context
        String context =
                results.stream()
                        .map(InsuranceDocumentSearchResultDTO::getContent)
                        .collect(Collectors.joining("\n\n"));

        // 4. Build prompt


        String prompt = """
                شما یک دستیار هوشمند حوزه بیمه هستید.

                فقط بر اساس اطلاعات موجود در Context
                به سؤال کاربر پاسخ بده.

                اگر پاسخ سؤال در Context وجود ندارد،
                صادقانه بگو که اطلاعات کافی در اسناد موجود نیست.

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);

        // 5. Ask LLM
        String answer =
                chatClient
                        .prompt(prompt)
                        .call()
                        .content();

        // 6. Return answer + sources

        /**
         *
         * Retrieved Knowledge
         *         +
         * LLM
         *         =
         * RAG Answer
         */
        return InsuranceRagResponseDTO.builder()
                .question(question)
                .answer(answer)
                .sources(results)
                .build();
    }
}



/** RAGService
 * Question
 *    ↓
 * Embedding
 *    ↓
 * Search PostgreSQL
 *    ↓
 * Take Top K content
 *    ↓
 * Build Context
 *    ↓
 * Send Context + Question to ChatClient
 *    ↓
 * Answer
 */

/**
 *
 * service
 *  ├── InsuranceDocumentService
 *  ├── InsuranceDocumentSearchService
 *  │
 *  └── InsuranceRagService
 */


/**
 *
 * پس در V7 اول RAG را با همین معماری خودمان می‌سازیم:
 *
 * JdbcTemplate
 *      +
 * pgvector
 *      +
 * EmbeddingModel
 *      +
 * ChatClient
 *
 * بعد که مفهوم RAG کاملاً جا افتاد، می‌توانیم ببینیم Spring AI PgVectorStore چطور همین کار را abstraction می‌کند.
 */



/** v6
 Question
 ↓
 Embedding
 ↓
 Vector Search
 ↓
 Documents


 V6 به تو می‌گوید:
 Document 1
 distance = 0.0903

 Document 2
 distance = 0.1476

 Document 6
 distance = 0.1840
 */





/** v7
 Question
 ↓
 Embedding
 ↓
 Vector Search
 ↓
 Relevant Documents
 ↓
 Context
 ↓
 LLM
 ↓
 Answer




 اما V7 باید از این اطلاعات استفاده کند:
 Context:

 شرایط بازنشستگی بیمه شده بر اساس سن و سابقه پرداخت حق بیمه تعیین می‌شود...

 شده برای شود. بیمهشده بر اساس مقررات مربوط به سن و سابقه پرداخت حق بیمه...





 و بعد به llama3.2 بگوید:
 با استفاده از Context زیر به سؤال کاربر پاسخ بده.

 Context:
 ...

 Question:
 شرایط بازنشستگی بیمه شده چیست؟





 LLM مثلاً پاسخ می‌دهد:

 شرایط بازنشستگی بر اساس سن و سابقه پرداخت حق بیمه تعیین می‌شود. همچنین بیمه‌شده باید حداقل سابقه لازم و شرایط سنی و قانونی مربوط به بازنشستگی را احراز کند.

 این دیگر RAG است.
 */