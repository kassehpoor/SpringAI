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

    /**
     {
     "requirement": "شرایط بازنشستگی بیمه شده چیست؟",
     "schema": "CLSU_CANPEN",
     "oracleObjectName": "LSU_REQ",
     "oracleObjectType": "TABLE",
     "maxDepth": 3,
     "topK": 5
     }

     */
    @PostMapping
    public RequirementTraceabilityV37Response analyze(
            @Valid @RequestBody RequirementTraceabilityV37Request request)
            throws IOException {

        return traceabilityService.analyze(request);
    }
}