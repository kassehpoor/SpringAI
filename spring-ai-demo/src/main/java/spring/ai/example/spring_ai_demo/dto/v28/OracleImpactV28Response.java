package spring.ai.example.spring_ai_demo.dto.v28;

import java.util.List;

public record OracleImpactV28Response(
        String projectName,
        String schema,
        String oracleObjectName,
        int javaMatchCount,
        List<JavaMatch> javaMatches,
        String analysisMode,
        String warning
) {
    public record JavaMatch(
            String className,
            String methodName,
            String path,
            int line,
            String evidence
    ) {}
}