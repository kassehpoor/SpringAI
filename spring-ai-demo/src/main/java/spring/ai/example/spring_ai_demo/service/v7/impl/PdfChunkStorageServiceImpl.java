package spring.ai.example.spring_ai_demo.service.v7.impl;

/**
 * @author Tahereh Kasehpoor
 */



import lombok.RequiredArgsConstructor;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v7.PdfChunkStorageResponseDTO;
import spring.ai.example.spring_ai_demo.service.v4.PdfDocumentService;
import spring.ai.example.spring_ai_demo.service.v7.PdfChunkStorageService;
import spring.ai.example.spring_ai_demo.service.v7.TextChunker;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfChunkStorageServiceImpl
        implements PdfChunkStorageService {

    private static final int EMBEDDING_DIMENSIONS = 768;
    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 100;

    private final PdfDocumentService pdfDocumentService;
    private final TextChunker textChunker;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public PdfChunkStorageResponseDTO savePdfWithChunks(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "PDF file must not be empty"
            );
        }

        // 1. استخراج متن PDF
        Document document =
                pdfDocumentService.createDocument(file);

        String content = document.getText();

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "PDF contains no extractable text"
            );
        }

        String fileName =
                (String) document.getMetadata().get("fileName");

        Integer pageCount =
                (Integer) document.getMetadata().get("pageCount");

        // 2. تقسیم متن به Chunk
        List<String> chunks = textChunker.chunk(
                content,
                CHUNK_SIZE,
                CHUNK_OVERLAP
        );

        if (chunks.isEmpty()) {
            throw new IllegalStateException(
                    "No chunks were generated from the PDF"
            );
        }

        // 3. تولید embedding سند اصلی برای حفظ ساختار V6
        float[] documentEmbedding =
                embeddingModel.embed(content);

        validateEmbedding(documentEmbedding);

        String documentSql = """
                INSERT INTO insurance_document
                    (content, embedding, file_name, page_count)
                VALUES
                    (?, CAST(? AS vector), ?, ?)
                RETURNING id
                """;

        Long documentId = jdbcTemplate.queryForObject(
                documentSql,
                Long.class,
                content,
                toVectorText(documentEmbedding),
                fileName,
                pageCount
        );

        if (documentId == null) {
            throw new IllegalStateException(
                    "Failed to create the parent document"
            );
        }

        // 4. آماده‌سازی دستور درج Chunk
        String chunkSql = """
                INSERT INTO insurance_document_chunk
                    (document_id,
                     chunk_index,
                     content,
                     page_number,
                     content_hash,
                     embedding,
                     embedding_model)
                VALUES
                    (?, ?, ?, ?, ?, CAST(? AS vector), ?)
                """;

        // 5. تولید embedding و ذخیره هر Chunk
        for (int i = 0; i < chunks.size(); i++) {

            String chunkText = chunks.get(i);

            float[] chunkEmbedding =
                    embeddingModel.embed(chunkText);

            validateEmbedding(chunkEmbedding);

            String hash = sha256(chunkText);

            jdbcTemplate.update(
                    chunkSql,
                    documentId,
                    i,
                    chunkText,
                    null, // فعلاً استخراج شماره صفحه نداریم
                    hash,
                    toVectorText(chunkEmbedding),
                    "nomic-embed-text"
            );
        }

        // 6. پاسخ API
        return PdfChunkStorageResponseDTO.builder()
                .documentId(documentId)
                .fileName(fileName)
                .pageCount(pageCount)
                .totalChunks(chunks.size())
                .embeddingDimensions(EMBEDDING_DIMENSIONS)
                .message(
                        "PDF and chunks saved successfully"
                )
                .build();
    }

    private void validateEmbedding(float[] embedding) {

        if (embedding == null
                || embedding.length != EMBEDDING_DIMENSIONS) {

            throw new IllegalStateException(
                    "Expected " + EMBEDDING_DIMENSIONS
                            + " embedding dimensions, but received "
                            + (embedding == null
                            ? "null"
                            : embedding.length)
            );
        }
    }

    private String toVectorText(float[] embedding) {
        return Arrays.toString(embedding);
    }

    private String sha256(String text) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    text.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    ex
            );
        }
    }
}
