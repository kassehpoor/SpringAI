package spring.ai.example.spring_ai_demo.service.v6.document.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentResponseDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.PdfEmbeddingResponseDTO;
import spring.ai.example.spring_ai_demo.service.v4.PdfDocumentService;
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
    private final PdfDocumentService pdfDocumentService;

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


    /**

     PDF
     ↓
     PdfParserService
     ↓
     PDFBox
     ↓
     String
     ↓
     Spring AI Document

     */
    @Override
    @Transactional
    public PdfEmbeddingResponseDTO savePdf(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "PDF file must not be empty"
            );
        }

        // 1. PDF → Spring AI Document
        Document document =
                pdfDocumentService.createDocument(file);

        // 2. Document → Embedding
        float[] embedding =
                embeddingModel.embed(document);

        // 3. بررسی dimension
        if (embedding.length != 768) {
            throw new IllegalStateException(
                    "Expected 768 dimensions, but received "
                            + embedding.length
            );
        }

        // 4. تبدیل vector به فرمت قابل استفاده توسط PostgreSQL
        String vectorText =
                java.util.Arrays.toString(embedding);

        String fileName =
                (String) document.getMetadata().get("fileName");

        Integer pageCount =
                (Integer) document.getMetadata().get("pageCount");

        // 5. ذخیره در PostgreSQL
        String sql = """
            INSERT INTO insurance_document
                (content, embedding, file_name, page_count)
            VALUES
                (?, CAST(? AS vector), ?, ?)
            RETURNING id
            """;

        Long id = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                document.getText(),
                vectorText,
                fileName,
                pageCount
        );

        return PdfEmbeddingResponseDTO.builder()
                .id(id)
                .fileName(fileName)
                .pageCount(pageCount)
                .embeddingDimensions(embedding.length)
                .message(
                        "PDF, embedding and metadata saved successfully"
                )
                .build();
    }
}

/**
 * ┌───────────────────────┐
 * │       PDF             │
 * └──────────┬────────────┘
 *            ↓
 * ┌───────────────────────┐
 * │       PDFBox          │
 * └──────────┬────────────┘
 *            ↓
 * ┌───────────────────────┐
 * │ Spring AI Document    │
 * │ text + metadata       │
 * └──────────┬────────────┘
 *            ↓
 * ┌───────────────────────┐
 * │   EmbeddingModel      │
 * │   nomic-embed-text    │
 * └──────────┬────────────┘
 *            ↓
 *       float[768]
 *            ↓
 * ┌───────────────────────┐
 * │      pgvector         │
 * │      VECTOR(768)      │
 * └──────────┬────────────┘
 *            ↓
 * ┌───────────────────────┐
 * │   insurance_document  │
 * │                       │
 * │ content               │
 * │ embedding             │
 * │ file_name             │
 * │ page_count            │
 * │ created_at             │
 * └───────────────────────┘
 */