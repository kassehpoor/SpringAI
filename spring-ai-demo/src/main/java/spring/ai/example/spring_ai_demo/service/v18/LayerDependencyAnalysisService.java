package spring.ai.example.spring_ai_demo.service.v18;

/**
 * @author Tahereh Kasehpoor
 */


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.Type;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class LayerDependencyAnalysisService {

    private final ProjectStructureIndexService indexService;

    public LayerDependencyAnalysisService(
            ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    public LayerReport analyze(String className, int requestedLimit) {
        ProjectStructureIndexService.ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException("No project is indexed.");
        }

        int limit = Math.max(1, Math.min(requestedLimit, 500));

        ProjectStructureIndexService.ClassInfo source = index.classes().values().stream()
                .filter(c -> c.qualifiedName().equals(className)
                        || c.name().equals(className))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Class not found: " + className));

        Path sourceFile = index.sourceDirectory()
                .resolve(source.path())
                .normalize();

        if (!sourceFile.startsWith(index.sourceDirectory().normalize())
                || !Files.isRegularFile(sourceFile)) {
            throw new IllegalStateException(
                    "Source file not found: " + source.path());
        }

        try {
            CompilationUnit cu =
                    StaticJavaParser.parse(sourceFile);

            Map<String, ProjectStructureIndexService.ClassInfo> byQualifiedName =
                    index.classes().values().stream()
                            .collect(Collectors.toMap(
                                    ProjectStructureIndexService.ClassInfo::qualifiedName,
                                    c -> c,
                                    (first, second) -> first));

            Map<String, List<ProjectStructureIndexService.ClassInfo>> bySimpleName =
                    index.classes().values().stream()
                            .collect(Collectors.groupingBy(
                                    ProjectStructureIndexService.ClassInfo::name));

            Map<String, String> imports = new HashMap<>();

            cu.getImports().stream()
                    .filter(i -> !i.isAsterisk() && !i.isStatic())
                    .forEach(i -> {
                        String qn = i.getNameAsString();
                        imports.put(
                                qn.substring(qn.lastIndexOf('.') + 1),
                                qn);
                    });

            Set<String> fieldTypes = new LinkedHashSet<>();

            for (FieldDeclaration field : cu.findAll(FieldDeclaration.class)) {
                for (var variable : field.getVariables()) {
                    String typeName = normalizeType(
                            variable.getType());

                    ProjectStructureIndexService.ClassInfo resolved = resolveClass(
                            typeName, imports, byQualifiedName,
                            bySimpleName);

                    if (resolved != null
                            && !resolved.qualifiedName()
                            .equals(source.qualifiedName())) {
                        fieldTypes.add(resolved.qualifiedName());
                    }
                }
            }

            Map<String, Set<String>> methodCalls = new LinkedHashMap<>();

            for (MethodDeclaration method :
                    cu.findAll(MethodDeclaration.class)) {

                Set<String> targets = new LinkedHashSet<>();

                for (MethodCallExpr call :
                        method.findAll(MethodCallExpr.class)) {

                    if (call.getScope().isEmpty()) {
                        continue;
                    }

                    String scope = call.getScope().get().toString();
                    String firstPart = scope.split("\\.")[0];

                    ProjectStructureIndexService.ClassInfo target = resolveClass(
                            firstPart, imports, byQualifiedName,
                            bySimpleName);

                    if (target != null) {
                        targets.add(target.qualifiedName());
                    }
                }

                methodCalls.put(method.getNameAsString(), targets);
            }

            List<Dependency> dependencies = new ArrayList<>();

            for (String targetName : fieldTypes) {
                ClassInfo target = byQualifiedName.get(targetName);

                dependencies.add(new Dependency(
                        source.qualifiedName(),
                        targetName,
                        classify(source),
                        classify(target),
                        "FIELD_TYPE",
                        "HIGH"));
            }

            for (var entry : methodCalls.entrySet()) {
                for (String targetName : entry.getValue()) {
                    ClassInfo target = byQualifiedName.get(targetName);

                    if (target == null
                            || fieldTypes.contains(targetName)) {
                        continue;
                    }

                    dependencies.add(new Dependency(
                            source.qualifiedName(),
                            targetName,
                            classify(source),
                            classify(target),
                            "METHOD_SCOPE",
                            "MEDIUM"));
                }
            }

            List<Dependency> limited = dependencies.stream()
                    .distinct()
                    .limit(limit)
                    .toList();

            return new LayerReport(
                    index.projectName(),
                    source.qualifiedName(),
                    classify(source),
                    dependencies.size(),
                    limited.size(),
                    dependencies.size() > limit,
                    limited,
                    "Static source analysis only; dependencies do not prove runtime execution."
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not analyze class: " + source.qualifiedName(),
                    e);
        }
    }

    private ClassInfo resolveClass(
            String typeName,
            Map<String, String> imports,
            Map<String, ClassInfo> byQualifiedName,
            Map<String, List<ClassInfo>> bySimpleName) {

        if (typeName == null || typeName.isBlank()) {
            return null;
        }

        ClassInfo exact = byQualifiedName.get(typeName);
        if (exact != null) {
            return exact;
        }

        String importedName = imports.get(typeName);
        if (importedName != null) {
            exact = byQualifiedName.get(importedName);
            if (exact != null) {
                return exact;
            }
        }

        List<ClassInfo> candidates = bySimpleName.get(typeName);

        if (candidates != null && candidates.size() == 1) {
            return candidates.get(0);
        }

        return null;
    }

    private String normalizeType(Type type) {
        String name = type.toString();

        // Handle common generic types, e.g. List<MyService>.
        int genericStart = name.indexOf('<');
        if (genericStart >= 0) {
            name = name.substring(genericStart + 1);
            int genericEnd = name.indexOf('>');
            if (genericEnd >= 0) {
                name = name.substring(0, genericEnd);
            }
        }

        while (name.endsWith("[]")) {
            name = name.substring(0, name.length() - 2);
        }

        return name.trim();
    }

    private String classify(ClassInfo info) {
        if (info == null) {
            return "UNKNOWN";
        }

        String name = (info.packageName() + "." + info.name())
                .toLowerCase(Locale.ROOT);

        if (name.contains(".rest.")
                || name.contains(".controller.")
                || name.contains("restservice")) {
            return "REST";
        }

        if (name.contains(".service.")
                || name.endsWith("service")
                || name.endsWith("manager")
                || name.endsWith("facade")) {
            return "SERVICE";
        }

        if (name.contains(".dao.")
                || name.contains(".repository.")
                || name.endsWith("repository")
                || name.endsWith("dao")) {
            return "DAO";
        }

        if (name.contains(".entity.")
                || name.contains(".model.")
                || name.endsWith("entity")) {
            return "ENTITY";
        }

        return "OTHER";
    }

    public record Dependency(
            String sourceClass,
            String targetClass,
            String sourceLayer,
            String targetLayer,
            String evidence,
            String confidence) {
    }

    public record LayerReport(
            String projectName,
            String analyzedClass,
            String analyzedLayer,
            int totalDependenciesFound,
            int returnedDependencies,
            boolean truncated,
            List<Dependency> dependencies,
            String note) {
    }
}

