package spring.ai.example.spring_ai_demo.service.v7.impl;

/**
 * @author Tahereh Kasehpoor
 */



import lombok.RequiredArgsConstructor;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchResultDTO;
import spring.ai.example.spring_ai_demo.service.v7.ChunkSearchService;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChunkSearchServiceImpl
        implements ChunkSearchService {

    private static final int EMBEDDING_DIMENSIONS = 768;
    private static final int MAX_TOP_K = 20;

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<ChunkSearchResultDTO> search(
            ChunkSearchRequestDTO request) {

        // 1. اعتبارسنجی درخواست
        if (request == null
                || request.getQuery() == null
                || request.getQuery().isBlank()) {

            throw new IllegalArgumentException(
                    "Query must not be blank"
            );
        }

        Integer topK = request.getTopK();

        if (topK == null || topK < 1 || topK > MAX_TOP_K) {
            throw new IllegalArgumentException(
                    "topK must be between 1 and "
                            + MAX_TOP_K
            );
        }

        String query = request.getQuery().trim();

        // 2. تبدیل سؤال به embedding
        float[] queryEmbedding =
                embeddingModel.embed(query);

        if (queryEmbedding == null
                || queryEmbedding.length != EMBEDDING_DIMENSIONS) {

            throw new IllegalStateException(
                    "Expected " + EMBEDDING_DIMENSIONS
                            + " embedding dimensions"
            );
        }

        String vectorText =
                Arrays.toString(queryEmbedding);


        /**
         عبارت زیر در PostgreSQL با عملگر pgvector فاصله کسینوسی را محاسبه می‌کند:
         c.embedding <=> q.embedding
         */
        // 3. جست‌وجوی نزدیک‌ترین Chunkها
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

        // 4. تبدیل نتیجه SQL به DTO
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    double distance =
                            rs.getDouble("distance");

                    return ChunkSearchResultDTO.builder()
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
                            .cosineSimilarity(1.0 - distance)
                            .build();
                },
                vectorText,
                topK
        );
    }
}

