package spring.ai.example.spring_ai_demo.controller.v6.document;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentResponseDTO;
import spring.ai.example.spring_ai_demo.service.v6.document.InsuranceDocumentService;

/**
 * @author Tahereh Kasehpoor
 */


@RestController
@RequestMapping("/api/v6/documents")
@RequiredArgsConstructor
public class InsuranceDocumentController {

    private final InsuranceDocumentService documentService;

    @PostMapping
    public InsuranceDocumentResponseDTO save(
             @RequestBody InsuranceDocumentRequestDTO request) {

        return documentService.save(request);
    }
}