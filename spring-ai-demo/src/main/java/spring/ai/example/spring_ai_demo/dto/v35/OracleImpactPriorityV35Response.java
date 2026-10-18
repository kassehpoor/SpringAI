
package spring.ai.example.spring_ai_demo.dto.v35;

import java.util.List;

public record OracleImpactPriorityV35Response(
        String projectName,
        String schema,
        int objectCount,
        List<PrioritizedObject> priorities,
        String warning
) {
    public record PrioritizedObject(
            int rank,
            String objectName,
            String objectType,
            String status,
            String lastDdlTime,
            int javaMatchCount,
            int affectedClassCount,
            String reviewPriority
    ) {}
}
