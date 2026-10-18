
package spring.ai.example.spring_ai_demo.service.v32;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v32.OracleDependencyCascadeV32Response;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

import java.util.*;

@Service
public class OracleDependencyCascadeV32Service {

    private final OracleMetadataService metadataService;

    public OracleDependencyCascadeV32Service(
            OracleMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    public OracleDependencyCascadeV32Response analyze(
            String schema,
            String objectName,
            String objectType,
            int maxDepth) {

        if (schema == null || schema.isBlank()) {
            throw new IllegalArgumentException("schema is required.");
        }
        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("objectName is required.");
        }
        if (objectType == null || objectType.isBlank()) {
            throw new IllegalArgumentException("objectType is required.");
        }
        if (maxDepth < 1 || maxDepth > 10) {
            throw new IllegalArgumentException(
                    "maxDepth must be between 1 and 10.");
        }

        String rootOwner = schema.trim().toUpperCase(Locale.ROOT);
        String rootName = objectName.trim().toUpperCase(Locale.ROOT);
        String rootType = objectType.trim().toUpperCase(Locale.ROOT);

        Map<String, OracleDependencyCascadeV32Response.DependencyNode>
                visited = new LinkedHashMap<>();

        List<OracleDependencyCascadeV32Response.DependencyEdge> edges =
                new ArrayList<>();

        Deque<OracleDependencyCascadeV32Response.DependencyNode> queue =
                new ArrayDeque<>();

        var root = new OracleDependencyCascadeV32Response.DependencyNode(
                rootOwner, rootName, rootType, 0);

        visited.put(key(rootOwner, rootName, rootType), root);
        queue.add(root);

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();

            if (current.depth() >= maxDepth) {
                continue;
            }

            List<Map<String, Object>> rows =
                    metadataService.listDependencies(
                            current.owner(), current.name(), current.type());

            for (Map<String, Object> row : rows) {
                String owner = value(row, "OWNER");
                String name = value(row, "NAME");
                String type = value(row, "TYPE");
                String referencedOwner = value(row, "REFERENCED_OWNER");
                String referencedName = value(row, "REFERENCED_NAME");
                String referencedType = value(row, "REFERENCED_TYPE");
                String dependencyType = value(row, "DEPENDENCY_TYPE");

                if (referencedOwner == null
                        || referencedName == null
                        || referencedType == null) {
                    continue;
                }

                int nextDepth = current.depth() + 1;

                edges.add(
                        new OracleDependencyCascadeV32Response.DependencyEdge(
                                owner, name, type,
                                referencedOwner, referencedName,
                                referencedType, dependencyType, nextDepth));

                String referencedKey = key(
                        referencedOwner, referencedName, referencedType);

                if (!visited.containsKey(referencedKey)) {
                    var node =
                            new OracleDependencyCascadeV32Response.DependencyNode(
                                    referencedOwner, referencedName,
                                    referencedType, nextDepth);

                    visited.put(referencedKey, node);
                    queue.addLast(node);
                }
            }
        }

        return new OracleDependencyCascadeV32Response(
                rootOwner,
                rootName,
                rootType,
                maxDepth,
                visited.size(),
                edges.size(),
                List.copyOf(visited.values()),
                List.copyOf(edges)
        );
    }

    private String key(String owner, String name, String type) {
        return (owner + "." + name + "." + type)
                .toUpperCase(Locale.ROOT);
    }

    private String value(Map<String, Object> row, String column) {
        Object result = row.get(column);

        if (result == null) {
            result = row.entrySet().stream()
                    .filter(entry -> entry.getKey()
                            .equalsIgnoreCase(column))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return result == null
                ? null
                : result.toString().trim().toUpperCase(Locale.ROOT);
    }
}
