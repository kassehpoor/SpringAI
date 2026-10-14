package spring.ai.example.spring_ai_demo.service.v22;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.MethodCallInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;


/**

این کلاس، نام اشیای Oracle را با متن فایل‌های Java و اطلاعات فراخوانی‌های ایندکس‌شده مقایسه
 می‌کند. نتایج به‌عنوان شواهد متنی گزارش می‌شوند؛ نه اثبات اجرای واقعی در زمان اجرا.
 */

@Service
@ConditionalOnProperty(
        prefix = "app.oracle",
        name = "enabled",
        havingValue = "true"
)
public class OracleUsageAnalysisService {

    private final ProjectStructureIndexService indexService;
    private final OracleMetadataService oracleService;

    public OracleUsageAnalysisService(
            ProjectStructureIndexService indexService,
            OracleMetadataService oracleService) {
        this.indexService = indexService;
        this.oracleService = oracleService;
    }

    public Map<String, Object> summary(String schema) {
        ProjectIndex index = requireIndex();

        List<Map<String, Object>> objects =
                oracleService.listObjects(schema, null, null);

        long packages = objects.stream()
                .filter(o -> "PACKAGE".equalsIgnoreCase(
                        stringValue(o, "OBJECT_TYPE")))
                .count();

        long procedures = objects.stream()
                .filter(o -> "PROCEDURE".equalsIgnoreCase(
                        stringValue(o, "OBJECT_TYPE")))
                .count();

        long functions = objects.stream()
                .filter(o -> "FUNCTION".equalsIgnoreCase(
                        stringValue(o, "OBJECT_TYPE")))
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectName", index.projectName());
        result.put("sourceFileCount", index.sourceFileCount());
        result.put("parsedFileCount", index.parsedFileCount());
        result.put("javaClassCount", index.classes().size());
        result.put("javaMethodCallCount", index.calls().size());
        result.put("javaParseErrorCount", index.errors().size());
        result.put("oracleVisibleObjectCount", objects.size());
        result.put("oraclePackageCount", packages);
        result.put("oracleProcedureCount", procedures);
        result.put("oracleFunctionCount", functions);
        result.put("analysisMode", "STATIC_TEXT_AND_INDEX");
        result.put("note",
                "Textual matches are evidence for manual review, not proof of runtime execution.");

        return result;
    }

    public List<Map<String, Object>> listOracleObjects(
            String schema,
            String type,
            String name) {

        return oracleService.listObjects(schema, type, name);
    }

    public Map<String, Object> findOracleUsage(
            String schema,
            String objectName) throws IOException {

        String normalizedName = requiredName(objectName);
        ProjectIndex index = requireIndex();

        List<Map<String, Object>> sourceMatches =
                findTextInJavaSources(index, normalizedName);

        List<Map<String, Object>> callMatches =
                findMatchesInMethodCalls(index, normalizedName);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectName", index.projectName());
        result.put("schema", schema);
        result.put("oracleObjectName", normalizedName);
        result.put("sourceTextMatchCount", sourceMatches.size());
        result.put("indexedCallMatchCount", callMatches.size());
        result.put("sourceTextMatches", sourceMatches);
        result.put("indexedCallMatches", callMatches);
        result.put("interpretation",
                "A name match is a static clue only. Confirm the actual SQL, JDBC call, " +
                        "framework mapping, and runtime behavior manually.");

        return result;
    }

    public List<Map<String, Object>> findUnmatched(
            String schema,
            String type,
            int requestedLimit) throws IOException {

        ProjectIndex index = requireIndex();

        int limit = Math.max(1, Math.min(requestedLimit, 200));
        List<Map<String, Object>> objects =
                oracleService.listObjects(schema, type, null);

        List<Map<String, Object>> unmatched = new ArrayList<>();

        for (Map<String, Object> object : objects) {
            if (unmatched.size() >= limit) {
                break;
            }

            String objectName = stringValue(object, "OBJECT_NAME");

            if (objectName == null || objectName.isBlank()) {
                continue;
            }

            List<Map<String, Object>> matches =
                    findTextInJavaSources(index, objectName);

            if (matches.isEmpty()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("owner", stringValue(object, "OWNER"));
                item.put("objectName", objectName);
                item.put("objectType", stringValue(object, "OBJECT_TYPE"));
                item.put("status", stringValue(object, "STATUS"));
                item.put("assessment",
                        "No case-insensitive textual occurrence found in Java source files.");
                item.put("warning",
                        "This does not prove the object is unused; names may be constructed dynamically or referenced indirectly.");
                unmatched.add(item);
            }
        }

        return unmatched;
    }

    private List<Map<String, Object>> findTextInJavaSources(
            ProjectIndex index,
            String searchTerm) throws IOException {

        Path sourceDirectory = index.sourceDirectory()
                .toAbsolutePath()
                .normalize();

        if (!Files.isDirectory(sourceDirectory)) {
            throw new IllegalStateException(
                    "Project source directory does not exist: " + sourceDirectory);
        }

        String needle = searchTerm.toUpperCase(Locale.ROOT);
        List<Map<String, Object>> matches = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            List<Path> javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT).endsWith(".java"))
                    .sorted()
                    .toList();

            for (Path file : javaFiles) {
                String relativePath = sourceDirectory.relativize(file)
                        .toString().replace('\\', '/');

                List<String> lines = Files.readAllLines(
                        file, StandardCharsets.UTF_8);

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);

                    if (line.toUpperCase(Locale.ROOT).contains(needle)) {
                        Map<String, Object> match = new LinkedHashMap<>();
                        match.put("path", relativePath);
                        match.put("line", i + 1);
                        match.put("text", line.trim());
                        match.put("evidenceType", "SOURCE_TEXT");
                        matches.add(match);

                        if (matches.size() >= 1000) {
                            return matches;
                        }
                    }
                }
            }
        }

        return matches;
    }

    private List<Map<String, Object>> findMatchesInMethodCalls(
            ProjectIndex index,
            String searchTerm) {

        String needle = searchTerm.toUpperCase(Locale.ROOT);
        List<Map<String, Object>> matches = new ArrayList<>();

        for (MethodCallInfo call : index.calls()) {
            boolean nameMatches = call.methodName() != null
                    && call.methodName().toUpperCase(Locale.ROOT)
                    .contains(needle);

            boolean scopeMatches = call.scope() != null
                    && call.scope().toUpperCase(Locale.ROOT)
                    .contains(needle);

            if (nameMatches || scopeMatches) {
                Map<String, Object> match = new LinkedHashMap<>();
                match.put("path", call.path());
                match.put("sourceClass", call.sourceClass());
                match.put("sourceMethod", call.sourceMethod());
                match.put("methodName", call.methodName());
                match.put("scope", call.scope());
                match.put("line", call.line());
                match.put("evidenceType", "INDEXED_METHOD_CALL");
                matches.add(match);

                if (matches.size() >= 1000) {
                    break;
                }
            }
        }

        return matches;
    }

    private ProjectIndex requireIndex() {
        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "No indexed Java project is loaded. Build or restore the project index first.");
        }

        return index;
    }

    private String requiredName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Oracle object name is required.");
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String stringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : value.toString();
    }
}
