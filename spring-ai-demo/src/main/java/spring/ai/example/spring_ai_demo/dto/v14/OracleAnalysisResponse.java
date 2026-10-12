package spring.ai.example.spring_ai_demo.dto.v14;

/**
 * @author Tahereh Kasehpoor
 */

import java.util.List;

public record OracleAnalysisResponse(
        String projectName,
        int totalFilesScanned,
        int totalReferencesFound,
        List<Reference> references
) {
    public record Reference(
            String path,
            int line,
            String category,
            String objectName,
            String snippet
    ) {}
}

