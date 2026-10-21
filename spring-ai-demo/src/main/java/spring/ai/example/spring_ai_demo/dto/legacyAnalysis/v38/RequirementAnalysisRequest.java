
package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v38;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequirementAnalysisRequest(
        @NotBlank
        @Size(max = 500)
        String documentName,

        @Size(max = 1000)
        String sourceReference,

        @NotBlank
        @Size(max = 100000)
        String requirementText
) {}
