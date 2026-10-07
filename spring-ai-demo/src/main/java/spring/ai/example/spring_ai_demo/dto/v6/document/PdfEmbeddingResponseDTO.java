package spring.ai.example.spring_ai_demo.dto.v6.document;

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
public class PdfEmbeddingResponseDTO {

    private Long id;

    private String fileName;

    private Integer pageCount;

    private int embeddingDimensions;

    private String message;
}