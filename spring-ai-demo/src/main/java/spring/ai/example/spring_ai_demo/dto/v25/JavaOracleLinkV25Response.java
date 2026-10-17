package spring.ai.example.spring_ai_demo.dto.v25;

import java.util.List;

public record JavaOracleLinkV25Response(
        String projectName,
        String schema,
        String oracleObject,
        int matchedSourceFiles,
        List<Match> matches
) {
    public record Match(
            String className,
            String methodName,
            String path,
            int line,
            String evidence
    ) {
    }
}