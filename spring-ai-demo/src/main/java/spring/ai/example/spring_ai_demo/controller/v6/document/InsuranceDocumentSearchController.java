package spring.ai.example.spring_ai_demo.controller.v6.document;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchResponseDTO;
import spring.ai.example.spring_ai_demo.service.v6.document.InsuranceDocumentSearchService;

/**
 * @author Tahereh Kasehpoor
 */

@RestController
@RequestMapping("/api/v6/documents")
@RequiredArgsConstructor
public class InsuranceDocumentSearchController {

    private final InsuranceDocumentSearchService searchService;

    @PostMapping("/search")
    public InsuranceDocumentSearchResponseDTO search(
            @Valid @RequestBody InsuranceDocumentSearchRequestDTO request) {

        return searchService.search(request);
    }
}