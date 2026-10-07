package spring.ai.example.spring_ai_demo.controller.v7;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.InsuranceRagResponseDTO;
import spring.ai.example.spring_ai_demo.service.v7.InsuranceRagService;

/**
 * @author Tahereh Kasehpoor
 */

@RestController
@RequestMapping("/api/v7/rag")
@RequiredArgsConstructor
public class InsuranceRagController {

    private final InsuranceRagService ragService;

    @PostMapping
    public InsuranceRagResponseDTO ask(
            @Valid @RequestBody InsuranceRagRequestDTO request) {

        return ragService.ask(request);
    }
}