package spring.ai.example.spring_ai_demo.service.v5;

import spring.ai.example.spring_ai_demo.dto.v5.TextSimilarityRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v5.TextSimilarityResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */
public interface TextSimilarityService {

    TextSimilarityResponseDTO calculate(
            TextSimilarityRequestDTO request
    );
}
