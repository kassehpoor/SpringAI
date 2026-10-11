
package spring.ai.example.spring_ai_demo.service.v7.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagResponseDTO;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagSourceDTO;
import spring.ai.example.spring_ai_demo.service.v7.InsuranceRagService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsuranceRagServiceImpl
        implements InsuranceRagService {

    private static final int EMBEDDING_DIMENSIONS = 768;
    private static final int MAX_TOP_K = 20;

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final ChatClient chatClient;

    @Override
    public InsuranceRagResponseDTO ask(
            InsuranceRagRequestDTO request) {

        // 1. Validate request
        if (request == null
                || request.getQuestion() == null
                || request.getQuestion().isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty"
            );
        }

        String question = request.getQuestion().trim();

        int topK = request.getTopK() == null
                ? 5
                : request.getTopK();

        if (topK < 1 || topK > MAX_TOP_K) {
            throw new IllegalArgumentException(
                    "topK must be between 1 and "
                            + MAX_TOP_K
            );
        }

        // 2. Create embedding for the question
        float[] queryEmbedding =
                embeddingModel.embed(question);

        if (queryEmbedding.length != EMBEDDING_DIMENSIONS) {
            throw new IllegalStateException(
                    "Expected "
                            + EMBEDDING_DIMENSIONS
                            + " embedding dimensions, but received "
                            + queryEmbedding.length
            );
        }

        String vectorText =
                Arrays.toString(queryEmbedding);

        // 3. Search relevant chunks
        String sql = """
                WITH query_vector AS (
                    SELECT CAST(? AS vector) AS embedding
                )
                SELECT
                    d.id AS document_id,
                    d.file_name,
                    c.chunk_index,
                    c.content,
                    c.page_number,
                    c.embedding <=> q.embedding AS distance
                FROM insurance_document_chunk c
                JOIN insurance_document d
                    ON d.id = c.document_id
                CROSS JOIN query_vector q
                ORDER BY c.embedding <=> q.embedding
                LIMIT ?
                """;

        List<InsuranceRagSourceDTO> sources =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> {
                            double distance =
                                    rs.getDouble("distance");

                            return InsuranceRagSourceDTO.builder()
                                    .documentId(
                                            rs.getLong("document_id")
                                    )
                                    .fileName(
                                            rs.getString("file_name")
                                    )
                                    .chunkIndex(
                                            rs.getInt("chunk_index")
                                    )
                                    .content(
                                            rs.getString("content")
                                    )
                                    .pageNumber(
                                            rs.getObject(
                                                    "page_number",
                                                    Integer.class
                                            )
                                    )
                                    .distance(distance)
                                    .cosineSimilarity(
                                            1.0 - distance
                                    )
                                    .build();
                        },
                        vectorText,
                        topK
                );

        // 4. Handle the case where no chunks exist
        if (sources.isEmpty()) {
            return InsuranceRagResponseDTO.builder()
                    .question(question)
                    .answer(
                            "در اسناد موجود، اطلاعاتی برای پاسخ "
                                    + "به این سؤال پیدا نشد."
                    )
                    .sources(sources)
                    .build();
        }

        // 5. Build context with source identifiers
        String context = sources.stream()
                .map(source -> """
                        [منبع: %s، شناسه سند: %d، شماره قطعه: %d]
                        %s
                        """.formatted(
                        source.getFileName(),
                        source.getDocumentId(),
                        source.getChunkIndex(),
                        source.getContent()
                ))
                .collect(Collectors.joining("\n\n"));

        // 6. Build a grounded prompt
        String prompt = """
                شما یک دستیار هوشمند حوزه بیمه هستید.

                دستورالعمل‌ها:
                - فقط بر اساس محتوای منابع زیر پاسخ بده.
                - اطلاعاتی را که در منابع وجود ندارند، حدس نزن.
                - اگر منابع برای پاسخ کافی نیستند، صریحاً اعلام کن.
                - بین اطلاعات صریح منبع و برداشت احتمالی تفاوت بگذار.
                - پاسخ را به زبان فارسی و روشن ارائه کن.
                - شناسه یا نام منبعی را که در منابع زیر وجود ندارد
                  جعل نکن.

                منابع:
                %s

                پرسش کاربر:
                %s
                """.formatted(context, question);

        // 7. Ask the LLM
        String answer = chatClient
                .prompt(prompt)
                .call()
                .content();

        if (answer == null || answer.isBlank()) {
            answer = "مدل نتوانست پاسخ متنی تولید کند.";
        }

        // 8. Return answer and retrieved sources
        return InsuranceRagResponseDTO.builder()
                .question(question)
                .answer(answer)
                .sources(sources)
                .build();
    }
}
