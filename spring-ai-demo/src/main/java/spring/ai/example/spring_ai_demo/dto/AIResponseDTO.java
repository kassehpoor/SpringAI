package spring.ai.example.spring_ai_demo.dto;

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
public class AIResponseDTO {

    private String answer;
}