package spring.ai.example.spring_ai_demo.service.v6.document.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchResponseDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchResultDTO;
import spring.ai.example.spring_ai_demo.service.v6.document.InsuranceDocumentSearchService;

import java.util.List;

/**
 * @author Tahereh Kasehpoor
 */


/**
 Document
 ↓
 Embedding
 ↓
 VECTOR(768)




 Question
 ↓
 Embedding
 ↓
 VECTOR(768)
 */
@Service
@RequiredArgsConstructor
public class InsuranceDocumentSearchServiceImpl
        implements InsuranceDocumentSearchService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public InsuranceDocumentSearchResponseDTO search(
            InsuranceDocumentSearchRequestDTO request) {

        String query = request.getQuery().trim();

        // 1. تبدیل سؤال کاربر به embedding
        float[] queryEmbedding =
                embeddingModel.embed(query);

        // 2. اطمینان از dimension صحیح
        if (queryEmbedding.length != 768) {
            throw new IllegalStateException(
                    "Expected 768 dimensions, but received "
                            + queryEmbedding.length
            );
        }

        // 3. تبدیل embedding به قالب pgvector
        String vectorText =
                java.util.Arrays.toString(queryEmbedding);

        // 4. جستجوی نزدیک‌ترین vectorها

        /***
         *
         * embedding دیتابیس
         *         ↕
         * query embedding
         *         ↓
         * cosine distance
         *
         *
         *
         *
         *
         *
         *
         * distance = 0
         *      ↓
         * کاملاً هم‌جهت
         *
         * distance کوچک
         *      ↓
         * مشابه‌تر
         *
         * distance بزرگ
         *      ↓
         * کم‌شباهت‌تر
         */
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

        return InsuranceDocumentSearchResponseDTO.builder()
                .query(query)
                .topK(request.getTopK())
                .results(results)
                .build();
    }
}
