package spring.ai.example.spring_ai_demo.service.v5;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v5.EmbeddingResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */

public interface PdfEmbeddingService {

    float[] createEmbedding(MultipartFile file);
}
