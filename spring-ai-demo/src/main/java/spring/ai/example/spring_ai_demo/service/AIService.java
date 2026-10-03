package spring.ai.example.spring_ai_demo.service;

import spring.ai.example.spring_ai_demo.dto.AIRequestDTO;
import spring.ai.example.spring_ai_demo.dto.AIResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */

public interface AIService {

    AIResponseDTO ask(AIRequestDTO request);
}