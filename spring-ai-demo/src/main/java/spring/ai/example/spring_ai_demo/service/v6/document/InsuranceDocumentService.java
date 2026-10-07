package spring.ai.example.spring_ai_demo.service.v6.document;

import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */
public interface InsuranceDocumentService {

    InsuranceDocumentResponseDTO save(
            InsuranceDocumentRequestDTO request
    );
}
