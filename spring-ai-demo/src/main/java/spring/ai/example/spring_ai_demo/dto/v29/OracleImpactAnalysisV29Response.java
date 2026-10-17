
package spring.ai.example.spring_ai_demo.dto.v29;

import java.util.List;

public record OracleImpactAnalysisV29Response(
        String projectName,
        String schema,
        String oracleObjectName,
        int javaMatchCount,
        int affectedClassCount,
        List<AffectedJavaFile> affectedFiles,
        String analysisMode,
        String warning
) {
    public record AffectedJavaFile(
            String className,
            String methodName,
            String path,
            int line,
            String evidence
    ) {}
}
