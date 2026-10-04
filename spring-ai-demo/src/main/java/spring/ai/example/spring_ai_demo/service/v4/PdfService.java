package spring.ai.example.spring_ai_demo.service.v4;

import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v4.PdfUploadResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */
public interface PdfService {

    PdfUploadResponseDTO upload(MultipartFile file);


}
