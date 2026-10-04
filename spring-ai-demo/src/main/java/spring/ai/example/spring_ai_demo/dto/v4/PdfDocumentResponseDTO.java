package spring.ai.example.spring_ai_demo.dto.v4;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tahereh Kasehpoor
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfDocumentResponseDTO {

    private String fileName;

    private String contentType;

    private long fileSize;

    private int pageCount;

    private String text;
}
