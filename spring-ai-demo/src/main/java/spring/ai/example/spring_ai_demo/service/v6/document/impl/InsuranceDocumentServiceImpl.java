package spring.ai.example.spring_ai_demo.service.v6.document.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentResponseDTO;
import spring.ai.example.spring_ai_demo.service.v6.document.InsuranceDocumentService;

import java.util.Arrays;

/**
 * @author Tahereh Kasehpoor
 */
@Service
@RequiredArgsConstructor
public class InsuranceDocumentServiceImpl
        implements InsuranceDocumentService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public InsuranceDocumentResponseDTO save(
            InsuranceDocumentRequestDTO request) {

        String content = request.getContent().trim();

        // 1. تبدیل متن به embedding با Ollama
        float[] embedding = embeddingModel.embed(content);

        // 2. بررسی dimension واقعی مدل
        if (embedding.length != 768) {
            throw new IllegalStateException(
                    "Expected 768 dimensions, but received "
                            + embedding.length
            );
        }

        // 3. تبدیل آرایه Java به قالب متنی قابل‌فهم برای pgvector
        String vectorText = Arrays.toString(embedding);

        // 4. ذخیره متن و embedding در PostgreSQL
        String sql = """
                INSERT INTO insurance_document (content, embedding)
                VALUES (?, CAST(? AS vector))
                RETURNING id
                """;

        Long id = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                content,
                vectorText
        );

        // 5. ساخت پاسخ
        return InsuranceDocumentResponseDTO.builder()
                .id(id)
                .embeddingDimensions(embedding.length)
                .message("Document and embedding saved successfully")
                .build();
    }
}