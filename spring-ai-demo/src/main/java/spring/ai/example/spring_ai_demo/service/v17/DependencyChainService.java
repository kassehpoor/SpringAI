package spring.ai.example.spring_ai_demo.service.v17;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DependencyChainService {

    private final ProjectStructureIndexService indexService;

    public DependencyChainService(
            ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    public ChainReport analyze(
            String className,
            int requestedDepth,
            int requestedLimit) {

        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "ابتدا پروژه را در V12 نمایه‌سازی کنید.");
        }

        int maxDepth = Math.max(1, Math.min(requestedDepth, 5));
        int limit = Math.max(1, Math.min(requestedLimit, 500));

        ClassInfo start = index.classes().values().stream()
                .filter(c -> c.qualifiedName().equals(className)
                        || c.name().equals(className))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "کلاس پیدا نشد: " + className));

        Map<String, Set<String>> graph = buildImportGraph(index);

        List<ChainNode> nodes = new ArrayList<>();
        List<DependencyEdge> edges = new ArrayList<>();

        Set<String> visited = new LinkedHashSet<>();
        Set<String> currentLevel = new LinkedHashSet<>();

        visited.add(start.qualifiedName());
        currentLevel.add(start.qualifiedName());

        nodes.add(new ChainNode(
                start.qualifiedName(),
                start.path(),
                0
        ));

        boolean truncated = false;

        for (int depth = 1;
             depth <= maxDepth && !currentLevel.isEmpty();
             depth++) {

            Set<String> nextLevel = new LinkedHashSet<>();

            for (String source : currentLevel) {
                for (String target : graph.getOrDefault(
                        source, Set.of())) {

                    if (edges.size() >= limit) {
                        truncated = true;
                        break;
                    }

                    edges.add(new DependencyEdge(
                            source,
                            target,
                            depth,
                            "IMPORT"
                    ));

                    if (visited.add(target)) {
                        ClassInfo targetInfo = index.classes().get(target);

                        nodes.add(new ChainNode(
                                target,
                                targetInfo == null ? "" : targetInfo.path(),
                                depth
                        ));

                        nextLevel.add(target);
                    }
                }

                if (truncated) {
                    break;
                }
            }

            if (truncated) {
                break;
            }

            currentLevel = nextLevel;
        }

        return new ChainReport(
                index.projectName(),
                start.qualifiedName(),
                maxDepth,
                nodes.size(),
                edges.size(),
                truncated,
                "IMPORT",
                nodes,
                edges,
                "مسیرها بر اساس importهای داخلی پروژه ساخته شده‌اند؛ "
                        + "این خروجی اثبات‌کننده فراخوانی زمان اجرا نیست."
        );
    }

    private Map<String, Set<String>> buildImportGraph(
            ProjectIndex index) {

        Map<String, Set<String>> graph = new LinkedHashMap<>();

        Map<String, List<String>> simpleNameIndex = new LinkedHashMap<>();

        for (ClassInfo info : index.classes().values()) {
            simpleNameIndex.computeIfAbsent(
                    info.name(),
                    ignored -> new ArrayList<>()
            ).add(info.qualifiedName());
        }

        for (ClassInfo source : index.classes().values()) {
            Set<String> dependencies = new LinkedHashSet<>();

            for (String imported : source.imports()) {
                if (imported == null || imported.isBlank()) {
                    continue;
                }

                String importName = imported.trim();

                if (importName.endsWith(".*")) {
                    String packagePrefix = importName.substring(
                            0, importName.length() - 1);

                    for (ClassInfo candidate : index.classes().values()) {
                        if (candidate.qualifiedName()
                                .startsWith(packagePrefix)) {
                            dependencies.add(candidate.qualifiedName());
                        }
                    }

                    continue;
                }

                if (index.classes().containsKey(importName)) {
                    dependencies.add(importName);
                    continue;
                }

                int lastDot = importName.lastIndexOf('.');
                String simpleName = lastDot >= 0
                        ? importName.substring(lastDot + 1)
                        : importName;

                List<String> candidates =
                        simpleNameIndex.getOrDefault(
                                simpleName, List.of());

                for (String candidate : candidates) {
                    if (candidate.equals(importName)) {
                        dependencies.add(candidate);
                    }
                }
            }

            // وابستگی به خود کلاس را حذف می‌کنیم.
            dependencies.remove(source.qualifiedName());

            graph.put(source.qualifiedName(), dependencies);
        }

        return graph;
    }

    public record ChainNode(
            String className,
            String sourcePath,
            int depth
    ) {}

    public record DependencyEdge(
            String fromClass,
            String toClass,
            int depth,
            String evidence
    ) {}

    public record ChainReport(
            String projectName,
            String rootClass,
            int maxDepth,
            int nodeCount,
            int edgeCount,
            boolean truncated,
            String analysisType,
            List<ChainNode> nodes,
            List<DependencyEdge> edges,
            String note
    ) {}
}

