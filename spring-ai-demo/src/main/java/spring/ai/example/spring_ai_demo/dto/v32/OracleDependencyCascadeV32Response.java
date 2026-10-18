
package spring.ai.example.spring_ai_demo.dto.v32;

import java.util.List;

public record OracleDependencyCascadeV32Response(
        String schema,
        String rootObjectName,
        String rootObjectType,
        int maxDepth,
        int objectCount,
        int dependencyCount,
        List<DependencyNode> objects,
        List<DependencyEdge> dependencies
) {
    public record DependencyNode(
            String owner,
            String name,
            String type,
            int depth
    ) {}

    public record DependencyEdge(
            String owner,
            String name,
            String type,
            String referencedOwner,
            String referencedName,
            String referencedType,
            String dependencyType,
            int depth
    ) {}
}
