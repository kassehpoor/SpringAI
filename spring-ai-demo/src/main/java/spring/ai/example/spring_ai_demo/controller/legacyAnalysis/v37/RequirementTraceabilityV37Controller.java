package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v37;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v37.RequirementTraceabilityV37Request;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v37.RequirementTraceabilityV37Response;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v37.RequirementTraceabilityV37Service;

import java.io.IOException;

@RestController
@RequestMapping("/api/v37/requirement-traceability")
@RequiredArgsConstructor
public class RequirementTraceabilityV37Controller {

    private final RequirementTraceabilityV37Service traceabilityService;

    @PostMapping
    public RequirementTraceabilityV37Response analyze(
            @Valid @RequestBody RequirementTraceabilityV37Request request)
            throws IOException {

        return traceabilityService.analyze(request);
    }
}