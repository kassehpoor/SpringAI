package spring.ai.example.spring_ai_demo.service.v2v3;

import spring.ai.example.spring_ai_demo.dto.v2.AIRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v2.AIResponseDTO;
import spring.ai.example.spring_ai_demo.dto.v3.InsuranceAnalysisDTO;

/**
 * @author Tahereh Kasehpoor
 */

public interface AIService {

    AIResponseDTO ask(AIRequestDTO request);

    InsuranceAnalysisDTO analyzeInsuranceQuestion(AIRequestDTO request);
}