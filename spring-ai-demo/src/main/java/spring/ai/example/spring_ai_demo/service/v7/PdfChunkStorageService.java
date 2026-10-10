package spring.ai.example.spring_ai_demo.service.v7;

import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v7.PdfChunkStorageResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */
public interface PdfChunkStorageService {

    PdfChunkStorageResponseDTO savePdfWithChunks(
            MultipartFile file
    );
}
