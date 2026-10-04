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
public class ThreeTextSimilarityResponseDTO {

    private String textA;

    private String textB;

    private String textC;

    private double similarityAB;

    private double similarityAC;

    private double similarityBC;
}