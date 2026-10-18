
package spring.ai.example.spring_ai_demo.dto.v30;

import java.util.List;

public record JavaOracleCascadeV30Response(
        String projectName,
        String schema,
        int oracleObjectCount,
        int javaMatchCount,
        int affectedClassCount,
        List<ObjectImpact> objects,
        String analysisMode,
        String warning
) {
    public record ObjectImpact(
            String objectName,
            int matchCount,
            List<JavaMatch> matches
    ) {}

    public record JavaMatch(
            String className,
            String methodName,
            String path,
            int line,
            String evidence
    ) {}
}
