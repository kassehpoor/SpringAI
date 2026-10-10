package spring.ai.example.spring_ai_demo.controller.v7;

/**
 * @author Tahereh Kasehpoor
 */



import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v7.PdfChunkStorageResponseDTO;
import spring.ai.example.spring_ai_demo.service.v7.PdfChunkStorageService;


@RestController
@RequestMapping("/api/v7/chunks")
@RequiredArgsConstructor
public class PdfChunkStorageController {

    private final PdfChunkStorageService pdfChunkStorageService;

    @PostMapping(
            value = "/pdf",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public PdfChunkStorageResponseDTO uploadPdf(
            @RequestParam("file") MultipartFile file) {

        return pdfChunkStorageService.savePdfWithChunks(file);
    }
}

/**

 User Question
 |
 v
 EmbeddingModel
 nomic-embed-text
 |
 v
 Question Vector [768]
 |
 v
 PostgreSQL + pgvector
 Compare with Chunk Embeddings
 |
 v
 ORDER BY embedding <=> query_vector
 |
 v
 Top-K Relevant Chunks
 |
 v
 REST Response

 */