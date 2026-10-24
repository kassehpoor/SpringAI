package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v44;

import java.util.List;

public record DataModelProposalV44Request(
        Long analysisId,
        String schema,
        List<String> tableNames
) {}