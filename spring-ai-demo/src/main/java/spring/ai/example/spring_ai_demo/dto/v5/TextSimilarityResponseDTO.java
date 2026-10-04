package spring.ai.example.spring_ai_demo.dto.v5;

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
public class TextSimilarityResponseDTO {

    private String textA;

    private String textB;

    private double similarity;
}
