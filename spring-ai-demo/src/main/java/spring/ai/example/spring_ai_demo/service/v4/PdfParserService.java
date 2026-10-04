package spring.ai.example.spring_ai_demo.service.v4;

import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v4.PdfParseResultDTO;

/**
 * @author Tahereh Kasehpoor
 */
public interface PdfParserService {

    PdfParseResultDTO parse(MultipartFile file);
}
