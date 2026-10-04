package spring.ai.example.spring_ai_demo.service.v5;

import spring.ai.example.spring_ai_demo.dto.v5.ThreeTextSimilarityRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v5.ThreeTextSimilarityResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */

public interface ThreeTextSimilarityService {

    ThreeTextSimilarityResponseDTO calculate(
            ThreeTextSimilarityRequestDTO request
    );
}