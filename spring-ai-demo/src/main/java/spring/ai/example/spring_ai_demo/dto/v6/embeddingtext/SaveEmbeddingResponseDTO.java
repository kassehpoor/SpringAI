package spring.ai.example.spring_ai_demo.dto.v6.embeddingtext;

import lombok.Builder;
import lombok.Data;

/**
 * @author Tahereh Kasehpoor
 */

@Data
@Builder
public class SaveEmbeddingResponseDTO {

    private Long id;
    private String content;
    private int embeddingDimension;
    private String message;
}