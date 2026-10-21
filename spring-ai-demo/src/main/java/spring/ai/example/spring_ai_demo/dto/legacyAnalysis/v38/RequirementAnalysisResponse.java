
package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v38;

import java.time.OffsetDateTime;

public record RequirementAnalysisResponse(
        Long id,
        String documentName,
        String sourceReference,
        String status,
        String analysisJson,
        OffsetDateTime createdAt
) {}
