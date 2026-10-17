package spring.ai.example.spring_ai_demo.dto.v26;

import java.util.List;

public record DependencyGraphV26Response(
        String projectName,
        String schema,
        int nodeCount,
        int edgeCount,
        List<Node> nodes,
        List<Edge> edges
) {
    public record Node(
            String id,
            String type,
            String label
    ) {}

    public record Edge(
            String source,
            String target,
            String relation,
            String evidence
    ) {}
}