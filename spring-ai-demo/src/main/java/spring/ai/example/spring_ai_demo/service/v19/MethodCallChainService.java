package spring.ai.example.spring_ai_demo.service.v19;

/**
 * @author Tahereh Kasehpoor
 */


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MethodCallChainService {

    private final ProjectStructureIndexService indexService;

    public MethodCallChainService(ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    public MethodChainReport analyze(
            String className, int requestedLimit) {

        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException("No project is indexed.");
        }

        int limit = Math.max(1, Math.min(requestedLimit, 500));

        ClassInfo source = index.classes().values().stream()
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
            CompilationUnit cu = StaticJavaParser.parse(sourceFile);

            Map<String, ClassInfo> byQualifiedName =
                    index.classes().values().stream()
                            .collect(Collectors.toMap(
                                    ClassInfo::qualifiedName,
                                    c -> c,
                                    (first, second) -> first));

            Map<String, List<ClassInfo>> bySimpleName =
                    index.classes().values().stream()
                            .collect(Collectors.groupingBy(ClassInfo::name));

            Map<String, String> imports = new HashMap<>();

            cu.getImports().stream()
                    .filter(i -> !i.isAsterisk() && !i.isStatic())
                    .forEach(i -> {
                        String qn = i.getNameAsString();
                        imports.put(
                                qn.substring(qn.lastIndexOf('.') + 1), qn);
                    });

            // Map field variable name -> resolved project class
            Map<String, ClassInfo> fieldTypes = new HashMap<>();

            for (FieldDeclaration field : cu.findAll(FieldDeclaration.class)) {
                String typeName = field.getElementType().toString();

                ClassInfo resolved = resolveClass(
                        typeName, imports, byQualifiedName, bySimpleName);

                if (resolved != null) {
                    field.getVariables().forEach(variable ->
                            fieldTypes.put(variable.getNameAsString(), resolved));
                }
            }

            List<MethodCallEdge> edges = new ArrayList<>();

            for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
                for (MethodCallExpr call :
                        method.findAll(MethodCallExpr.class)) {

                    if (call.getScope().isEmpty()) {
                        continue;
                    }

                    String scope = call.getScope().get().toString();

                    // This version resolves calls on injected fields,
                    // e.g. historyReportService.createReport().
                    ClassInfo target = fieldTypes.get(scope);

                    if (target == null) {
                        // Also support calls written as ClassName.method()
                        String firstPart = scope.split("\\.")[0];
                        target = resolveClass(
                                firstPart, imports,
                                byQualifiedName, bySimpleName);
                    }

                    if (target == null
                            || target.qualifiedName().equals(source.qualifiedName())) {
                        continue;
                    }

                    edges.add(new MethodCallEdge(
                            source.qualifiedName(),
                            method.getNameAsString(),
                            target.qualifiedName(),
                            call.getNameAsString(),
                            scope,
                            call.getBegin().map(p -> p.line).orElse(-1),
                            "AST_SCOPE_OR_FIELD",
                            "STATIC_ANALYSIS"));
                }
            }

            List<MethodCallEdge> uniqueEdges = edges.stream()
                    .distinct()
                    .toList();

            List<MethodCallEdge> returned = uniqueEdges.stream()
                    .limit(limit)
                    .toList();

            return new MethodChainReport(
                    index.projectName(),
                    source.qualifiedName(),
                    uniqueEdges.size(),
                    returned.size(),
                    uniqueEdges.size() > limit,
                    returned,
                    "Calls are inferred from Java source syntax; runtime dispatch and reflection are not resolved."
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not analyze method calls for "
                            + source.qualifiedName(), e);
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

        String imported = imports.get(typeName);
        if (imported != null) {
            exact = byQualifiedName.get(imported);
            if (exact != null) {
                return exact;
            }
        }

        List<ClassInfo> candidates = bySimpleName.get(typeName);

        return candidates != null && candidates.size() == 1
                ? candidates.get(0)
                : null;
    }

    public record MethodCallEdge(
            String sourceClass,
            String sourceMethod,
            String targetClass,
            String targetMethod,
            String callScope,
            int line,
            String evidence,
            String analysisType) {
    }

    public record MethodChainReport(
            String projectName,
            String analyzedClass,
            int totalCallsFound,
            int returnedCalls,
            boolean truncated,
            List<MethodCallEdge> calls,
            String note) {
    }
}

