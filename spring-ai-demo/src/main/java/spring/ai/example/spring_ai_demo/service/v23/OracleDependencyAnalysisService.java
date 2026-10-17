
package spring.ai.example.spring_ai_demo.service.v23;

/**
 * @author Tahereh Kasehpoor
 */

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(
        prefix = "app.oracle",
        name = "enabled",
        havingValue = "true"
)
public class OracleDependencyAnalysisService {

    private static final int MAX_RESULTS = 2000;

    private static final Pattern SQL_IDENTIFIER =
            Pattern.compile("[A-Za-z][A-Za-z0-9_$#]*");

    private static final Pattern SQL_INDICATOR = Pattern.compile(
            "(?i)prepareCall|CallableStatement|prepareStatement|"
                    + "executeQuery|executeUpdate|execute\\s*\\(|"
                    + "\\bCALL\\b|\\bBEGIN\\b|\\bEXEC(?:UTE)?\\b"
    );

    private final ProjectStructureIndexService indexService;
    private final OracleMetadataService oracleService;

    public OracleDependencyAnalysisService(
            ProjectStructureIndexService indexService,
            OracleMetadataService oracleService) {
        this.indexService = indexService;
        this.oracleService = oracleService;
    }

    /**
     * Summary without scanning every Java line against every Oracle object.
     */
    public Map<String, Object> summary(String schema) throws IOException {
        ProjectIndex index = requireIndex();

        List<Map<String, Object>> objects =
                oracleService.listObjects(schema, null, null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectName", index.projectName());
        result.put("javaClassCount", index.classes().size());
        result.put("javaMethodCallCount", index.calls().size());
        result.put("oracleVisibleObjectCount", objects.size());
        result.put("schema", schema);
        result.put("analysisMode", "STATIC_SOURCE_ANALYSIS");
        result.put(
                "warning",
                "Summary contains metadata counts only. "
                        + "Source matches are calculated by the targeted endpoints."
        );

        return result;
    }

    /**
     * Find Java source lines mentioning a specific Oracle object.
     */
    public List<Map<String, Object>> javaToOracle(
            String schema,
            String objectName) throws IOException {

        String name = normalize(objectName);

        // Validate schema/object visibility and avoid searching arbitrary names.
        List<Map<String, Object>> objects =
                oracleService.listObjects(schema, null, name);

        boolean exactMatch = objects.stream().anyMatch(object ->
                name.equalsIgnoreCase(String.valueOf(object.get("OBJECT_NAME")))
        );

        if (!exactMatch) {
            return List.of();
        }

        return scanObjectUsage(requireIndex(), name);
    }

    /**
     * Find Java source references for an exact Oracle object name.
     */
    public List<Map<String, Object>> oracleToJava(
            String schema,
            String objectName) throws IOException {

        String name = normalize(objectName);

        List<Map<String, Object>> objects =
                oracleService.listObjects(schema, null, name);

        boolean exactMatch = objects.stream().anyMatch(object ->
                name.equalsIgnoreCase(String.valueOf(object.get("OBJECT_NAME")))
        );

        if (!exactMatch) {
            Map<String, Object> missing = new LinkedHashMap<>();
            missing.put("objectName", name);
            missing.put("matches", List.of());
            missing.put(
                    "message",
                    "No exact Oracle object-name match was found in the selected schema."
            );
            return List.of(missing);
        }

        return scanObjectUsage(requireIndex(), name);
    }

    /**
     * Find SQL/JDBC-like lines that do not mention any known Oracle object name.
     * This is a heuristic and may include false positives.
     */
    public List<Map<String, Object>> unresolved(
            String schema,
            int requestedLimit) throws IOException {

        int limit = Math.max(1, Math.min(requestedLimit, 200));

        ProjectIndex index = requireIndex();

        List<Map<String, Object>> objects =
                oracleService.listObjects(schema, null, null);

        Set<String> knownNames = new LinkedHashSet<>();

        for (Map<String, Object> object : objects) {
            Object value = object.get("OBJECT_NAME");

            if (value != null && !value.toString().isBlank()) {
                knownNames.add(value.toString().toUpperCase(Locale.ROOT));
            }
        }

        Path root = getSourceRoot(index);
        List<Map<String, Object>> results = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT).endsWith(".java"))
                    .sorted()
                    .toList();

            for (Path file : files) {
                String relative = root.relativize(file)
                        .toString().replace('\\', '/');

                List<String> lines =
                        Files.readAllLines(file, StandardCharsets.UTF_8);

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);

                    if (!SQL_INDICATOR.matcher(line).find()) {
                        continue;
                    }

                    String upper = line.toUpperCase(Locale.ROOT);

                    // Ignore lines that mention any known Oracle object.
                    boolean knownObjectFound = knownNames.stream()
                            .anyMatch(upper::contains);

                    if (knownObjectFound) {
                        continue;
                    }

                    Matcher matcher = SQL_IDENTIFIER.matcher(line);
                    List<String> identifiers = new ArrayList<>();

                    while (matcher.find()) {
                        identifiers.add(matcher.group());
                    }

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("path", relative);
                    item.put("line", i + 1);
                    item.put("sourceLine", line.trim());
                    item.put("candidateIdentifiers", identifiers);
                    item.put("evidenceType", "UNRESOLVED_SQL_CANDIDATE");
                    item.put("confidence", "LOW");
                    item.put(
                            "warning",
                            "No known Oracle object name was found on this line. "
                                    + "Review the surrounding SQL and Java code."
                    );

                    results.add(item);

                    if (results.size() >= limit) {
                        return results;
                    }
                }
            }
        }

        return results;
    }

    private List<Map<String, Object>> scanObjectUsage(
            ProjectIndex index,
            String requestedName) throws IOException {

        Path root = getSourceRoot(index);
        List<Map<String, Object>> results = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT).endsWith(".java"))
                    .sorted()
                    .toList();

            for (Path file : files) {
                String relative = root.relativize(file)
                        .toString().replace('\\', '/');

                List<String> lines =
                        Files.readAllLines(file, StandardCharsets.UTF_8);

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String upper = line.toUpperCase(Locale.ROOT);

                    if (!upper.contains(requestedName)) {
                        continue;
                    }

                    boolean jdbcCall =
                            upper.contains("PREPARECALL")
                                    || upper.contains("CALLABLESTATEMENT");

                    boolean sqlPattern = SQL_INDICATOR.matcher(line).find();

                    String evidence;

                    if (jdbcCall) {
                        evidence = "CALLABLE_STATEMENT";
                    } else if (sqlPattern) {
                        evidence = "DIRECT_SQL_REFERENCE";
                    } else {
                        evidence = "TEXTUAL_MATCH";
                    }

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("path", relative);
                    item.put("line", i + 1);
                    item.put("oracleObjectName", requestedName);
                    item.put("sourceLine", line.trim());
                    item.put("evidenceType", evidence);
                    item.put("confidence", switch (evidence) {
                        case "CALLABLE_STATEMENT" -> "MEDIUM";
                        case "DIRECT_SQL_REFERENCE" -> "MEDIUM";
                        default -> "LOW";
                    });

                    results.add(item);

                    if (results.size() >= MAX_RESULTS) {
                        return results;
                    }
                }
            }
        }

        return results;
    }

    private Path getSourceRoot(ProjectIndex index) {
        Path root = index.sourceDirectory().toAbsolutePath().normalize();

        if (!Files.isDirectory(root)) {
            throw new IllegalStateException(
                    "Project source directory is unavailable: " + root
            );
        }

        return root;
    }

    private ProjectIndex requireIndex() {
        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "No indexed Java project is loaded."
            );
        }

        return index;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Object name is required.");
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }
}
