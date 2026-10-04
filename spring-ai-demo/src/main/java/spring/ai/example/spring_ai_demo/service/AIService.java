package spring.ai.example.spring_ai_demo.service;

import spring.ai.example.spring_ai_demo.dto.AIRequestDTO;
import spring.ai.example.spring_ai_demo.dto.AIResponseDTO;
import spring.ai.example.spring_ai_demo.dto.InsuranceAnalysisDTO;

/**
 * @author Tahereh Kasehpoor
 */

public interface AIService {

    AIResponseDTO ask(AIRequestDTO request);

    InsuranceAnalysisDTO analyzeInsuranceQuestion(AIRequestDTO request);
}