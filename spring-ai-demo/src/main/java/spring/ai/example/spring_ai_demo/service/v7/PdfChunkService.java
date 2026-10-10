package spring.ai.example.spring_ai_demo.service.v7;

import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v7.PdfChunkResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */
public interface PdfChunkService {

    PdfChunkResponseDTO chunk(
            MultipartFile file
    );
}
