package spring.ai.example.spring_ai_demo.service.v7;

import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */
public interface InsuranceRagService {


    InsuranceRagResponseDTO ask(
            InsuranceRagRequestDTO request
    );
}
