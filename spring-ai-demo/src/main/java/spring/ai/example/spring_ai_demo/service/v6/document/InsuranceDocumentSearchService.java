package spring.ai.example.spring_ai_demo.service.v6.document;

import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */
public interface InsuranceDocumentSearchService {

    InsuranceDocumentSearchResponseDTO search(
            InsuranceDocumentSearchRequestDTO request
    );
}
