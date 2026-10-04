package spring.ai.example.spring_ai_demo.dto.v5;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tahereh Kasehpoor
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponseDTO {

    private String text;

    private int dimension;

    private List<Double> vector;
}
