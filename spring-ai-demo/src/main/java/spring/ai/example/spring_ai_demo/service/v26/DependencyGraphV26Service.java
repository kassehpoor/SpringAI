package spring.ai.example.spring_ai_demo.service.v26;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v26.DependencyGraphV26Response;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class DependencyGraphV26Service {

    private final ProjectStructureIndexService indexService;

    public DependencyGraphV26Service(
            ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    public DependencyGraphV26Response buildGraph(
            String schema,
            String objectName,
            int maxMatches) throws IOException {

        var index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "Project index is not ready.");
        }

        String normalizedObject = objectName.trim()
                .toUpperCase(Locale.ROOT);

        if (normalizedObject.isBlank()) {
            throw new IllegalArgumentException(
                    "objectName is required.");
        }

        String normalizedSchema =
                schema == null || schema.isBlank()
                        ? "DEFAULT"
                        : schema.trim().toUpperCase(Locale.ROOT);

        Map<String, DependencyGraphV26Response.Node> nodes =
                new LinkedHashMap<>();

        List<DependencyGraphV26Response.Edge> edges =
                new ArrayList<>();

        String oracleNodeId =
                "ORACLE:" + normalizedSchema + "." + normalizedObject;

        nodes.put(oracleNodeId,
                new DependencyGraphV26Response.Node(
                        oracleNodeId,
                        "ORACLE_OBJECT",
                        normalizedSchema + "." + normalizedObject
                ));

        Set<String> matchedFiles = new LinkedHashSet<>();

        int limit = Math.max(1, Math.min(maxMatches, 1000));

        for (ClassInfo classInfo : index.classes().values()) {
            if (matchedFiles.size() >= limit) {
                break;
            }

            Path file = index.sourceDirectory()
                    .resolve(classInfo.path())
                    .normalize();

            if (!file.startsWith(index.sourceDirectory().normalize())
                    || !Files.isRegularFile(file)) {
                continue;
            }

            List<String> lines = Files.readAllLines(file);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (!line.toUpperCase(Locale.ROOT)
                        .contains(normalizedObject)) {
                    continue;
                }

                if (matchedFiles.add(classInfo.path())) {
                    String javaNodeId =
                            "JAVA:" + classInfo.qualifiedName();

                    nodes.putIfAbsent(javaNodeId,
                            new DependencyGraphV26Response.Node(
                                    javaNodeId,
                                    "JAVA_CLASS",
                                    classInfo.qualifiedName()
                            ));

                    edges.add(
                            new DependencyGraphV26Response.Edge(
                                    javaNodeId,
                                    oracleNodeId,
                                    "TEXT_MATCH",
                                    classInfo.path() + ":" + (i + 1)
                                            + " - " + line.trim()
                            )
                    );
                }

                break;
            }
        }

        return new DependencyGraphV26Response(
                index.projectName(),
                normalizedSchema,
                nodes.size(),
                edges.size(),
                new ArrayList<>(nodes.values()),
                edges
        );
    }
}