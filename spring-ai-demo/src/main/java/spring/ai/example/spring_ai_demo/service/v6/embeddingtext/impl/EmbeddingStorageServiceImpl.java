package spring.ai.example.spring_ai_demo.service.v6.embeddingtext.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.ai.example.spring_ai_demo.dto.v6.embeddingtext.SaveEmbeddingRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.embeddingtext.SaveEmbeddingResponseDTO;
import spring.ai.example.spring_ai_demo.service.v6.embeddingtext.EmbeddingStorageService;

/**
 * @author Tahereh Kasehpoor
 */


@Service
@RequiredArgsConstructor
public class EmbeddingStorageServiceImpl implements EmbeddingStorageService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public SaveEmbeddingResponseDTO save(
            SaveEmbeddingRequestDTO request) {

        String content = request.getContent();

        // 1. متن را به embedding واقعی تبدیل می‌کنیم
        float[] vector = embeddingModel.embed(content);

        // 2. Dimension را بررسی می‌کنیم
        if (vector.length != 768) {
            throw new IllegalStateException(
                    "Expected 768 dimensions but got " + vector.length
            );
        }

        // 3. بردار را به قالب متنی pgvector تبدیل می‌کنیم
        String vectorText = toVectorText(vector);

        // 4. متن و embedding را در PostgreSQL ذخیره می‌کنیم
        Long id = jdbcTemplate.queryForObject(
                """
                INSERT INTO insurance_document (content, embedding)
                VALUES (?, CAST(? AS vector))
                RETURNING id
                """,
                Long.class,
                content,
                vectorText
        );

        return SaveEmbeddingResponseDTO.builder()
                .id(id)
                .content(content)
                .embeddingDimension(vector.length)
                .message("Embedding saved successfully")
                .build();
    }

    private String toVectorText(float[] vector) {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                result.append(",");
            }
            result.append(vector[i]);
        }
        result.append("]");
        return result.toString();
    }
}