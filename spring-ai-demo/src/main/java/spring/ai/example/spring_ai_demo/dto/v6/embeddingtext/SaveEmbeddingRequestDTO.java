package spring.ai.example.spring_ai_demo.dto.v6.embeddingtext;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Tahereh Kasehpoor
 */
@Data
public class SaveEmbeddingRequestDTO {

    @NotBlank
    private String content;
}