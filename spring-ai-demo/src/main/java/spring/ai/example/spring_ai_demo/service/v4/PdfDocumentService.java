package spring.ai.example.spring_ai_demo.service.v4;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v4.PdfDocumentResponseDTO;




public interface PdfDocumentService {

    PdfDocumentResponseDTO parse(MultipartFile file);
    Document createDocument(MultipartFile file);
}
