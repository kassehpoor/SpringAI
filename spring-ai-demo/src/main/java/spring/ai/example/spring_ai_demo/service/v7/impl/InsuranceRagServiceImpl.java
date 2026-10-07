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