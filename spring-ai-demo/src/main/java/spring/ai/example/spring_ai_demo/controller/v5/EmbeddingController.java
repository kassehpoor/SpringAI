package spring.ai.example.spring_ai_demo.controller.v5;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v5.EmbeddingResponseDTO;
import spring.ai.example.spring_ai_demo.service.v5.PdfEmbeddingService;

/**
 * @author Tahereh Kasehpoor
 */

/**
*
 V4
 │
 ▼
 MultipartFile
 │
 ▼
 PdfParserService
 │
 ▼
 PDFBox
 │
 ▼
 PDF Text
 │
 ▼
 Document
 ┌────┴────┐
 │         │
 text     metadata
 │
 ┌───────┼────────┐
 │       │        │
 fileName  type   pageCount
 │
 ▼
 V5 Embedding
 │
 ▼
 EmbeddingModel
 │
 ▼
 float[]
 │
 ▼
 Vector Embedding
 *
 */


@RestController
@RequestMapping("/api/v5/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

    private final PdfEmbeddingService embeddingService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public float[] createEmbedding(
            @RequestPart("file") MultipartFile file) {

        return embeddingService.createEmbedding(file);
    }
}

