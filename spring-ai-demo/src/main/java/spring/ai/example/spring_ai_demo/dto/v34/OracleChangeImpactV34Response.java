
package spring.ai.example.spring_ai_demo.dto.v34;

import java.util.List;

public record OracleChangeImpactV34Response(
        String projectName,
        String schema,
        int objectCount,
        int matchedObjectCount,
        List<ObjectChange> objects,
        String warning
) {
    public record ObjectChange(
            String objectName,
            String objectType,
            String status,
            String lastDdlTime,
            int javaMatchCount,
            int affectedClassCount,
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
