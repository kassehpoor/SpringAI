
package spring.ai.example.spring_ai_demo.dto.v36;

import java.util.List;

public record OracleImpactReportV36Response(
        String projectName,
        String schema,
        String rootObjectName,
        String rootObjectType,
        int oracleObjectCount,
        int javaMatchCount,
        int affectedClassCount,
        List<ImpactItem> items,
        String warning
) {
    public record ImpactItem(
            int depth,
            String owner,
            String objectName,
            String objectType,
            int javaMatchCount,
            int affectedClassCount,
            String reviewPriority,
            List<JavaEvidence> javaEvidence
    ) {}

    public record JavaEvidence(
            String className,
            String methodName,
            String path,
            int line,
            String evidence
    ) {}
}
