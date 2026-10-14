
package spring.ai.example.spring_ai_demo.service.v20;

import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.MethodCallInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class ProjectAnalysisFacadeService {

    private final ProjectStructureIndexService indexService;

    private static final Pattern ORACLE_REFERENCE_PATTERN = Pattern.compile(
            "(?i)\\b("
                    + "DBMS_[A-Z0-9_$]+"
                    + "|UTL_[A-Z0-9_$]+"
                    + "|[A-Z][A-Z0-9_$]*\\.[A-Z][A-Z0-9_$]*"
                    + ")\\b"
    );

    private static final Pattern SQL_PATTERN = Pattern.compile(
            "(?i)\\b(SELECT\\s+.+?\\s+FROM|INSERT\\s+INTO|UPDATE\\s+\\w+|"
                    + "DELETE\\s+FROM|MERGE\\s+INTO|CALL\\s+\\w+)\\b"
    );

    public ProjectAnalysisFacadeService(
            ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    private ProjectStructureIndexService.ProjectIndex requireIndex() {
        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "No project is indexed. Upload and index a project first.");
        }

        return index;
    }

    public ProjectSummary getSummary() {
        ProjectIndex index = requireIndex();

        long methodCount = index.classes().values().stream()
                .mapToLong(c -> c.methods().size())
                .sum();

        return new ProjectSummary(
                index.projectName(),
                index.sourceFileCount(),
                index.parsedFileCount(),
                index.classes().size(),
                methodCount,
                index.calls().size(),
                index.errors().size(),
                index.sourceDirectory().toString(),
                "V20 static analysis; Oracle database metadata is not queried."
        );
    }

    public List<ClassSearchResult> searchClasses(
            String query, int requestedLimit) {

        ProjectIndex index = requireIndex();
        int limit = normalizeLimit(requestedLimit);
        String q = safeQuery(query).toLowerCase(Locale.ROOT);

        return index.classes().values().stream()
                .filter(c -> q.isBlank()
                        || c.name().toLowerCase(Locale.ROOT).contains(q)
                        || c.qualifiedName().toLowerCase(Locale.ROOT).contains(q)
                        || c.packageName().toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparing(ClassInfo::qualifiedName))
                .limit(limit)
                .map(c -> new ClassSearchResult(
                        c.qualifiedName(),
                        c.name(),
                        c.kind(),
                        c.packageName(),
                        c.path(),
                        c.methods().size(),
                        c.imports().size(),
                        c.inheritance()))
                .toList();
    }

    public ClassDetail getClassDetail(String className) {
        ProjectIndex index = requireIndex();
        ClassInfo info = findClass(index, className);

        List<MethodCallInfo> calls = index.calls().stream()
                .filter(c -> info.qualifiedName().equals(c.sourceClass()))
                .limit(500)
                .toList();

        return new ClassDetail(
                index.projectName(),
                info.qualifiedName(),
                info.name(),
                info.kind(),
                info.packageName(),
                info.path(),
                info.imports(),
                info.inheritance(),
                info.methods(),
                calls.size(),
                calls
        );
    }

    public List<MethodSearchResult> searchMethods(
            String query, String className, int requestedLimit) {

        ProjectIndex index = requireIndex();
        int limit = normalizeLimit(requestedLimit);
        String q = safeQuery(query).toLowerCase(Locale.ROOT);
        String classFilter = safeQuery(className).toLowerCase(Locale.ROOT);

        List<MethodSearchResult> results = new ArrayList<>();

        for (ClassInfo info : index.classes().values()) {
            if (!classFilter.isBlank()
                    && !info.name().toLowerCase(Locale.ROOT).contains(classFilter)
                    && !info.qualifiedName().toLowerCase(Locale.ROOT)
                    .contains(classFilter)) {
                continue;
            }

            for (String method : info.methods()) {
                if (q.isBlank()
                        || method.toLowerCase(Locale.ROOT).contains(q)) {
                    results.add(new MethodSearchResult(
                            info.qualifiedName(),
                            info.name(),
                            method,
                            info.path()
                    ));

                    if (results.size() >= limit) {
                        return results;
                    }
                }
            }
        }

        return results;
    }

    public List<MethodCallInfo> searchCalls(
            String methodName, String className, int requestedLimit) {

        ProjectIndex index = requireIndex();
        int limit = normalizeLimit(requestedLimit);

        String methodFilter = safeQuery(methodName).toLowerCase(Locale.ROOT);
        String classFilter = safeQuery(className).toLowerCase(Locale.ROOT);

        return index.calls().stream()
                .filter(c -> methodFilter.isBlank()
                        || c.methodName().toLowerCase(Locale.ROOT)
                        .contains(methodFilter))
                .filter(c -> classFilter.isBlank()
                        || c.sourceClass().toLowerCase(Locale.ROOT)
                        .contains(classFilter))
                .limit(limit)
                .toList();
    }

    public OracleStaticReport analyzeOracleReferences(
            int requestedLimit) throws IOException {

        ProjectIndex index = requireIndex();
        int limit = normalizeLimit(requestedLimit);

        List<OracleReference> references = new ArrayList<>();
        Set<String> visitedFiles = new LinkedHashSet<>();

        for (ClassInfo info : index.classes().values()) {
            Path file = index.sourceDirectory()
                    .resolve(info.path())
                    .normalize();

            if (!file.startsWith(index.sourceDirectory().normalize())
                    || !Files.isRegularFile(file)
                    || !visitedFiles.add(file.toString())) {
                continue;
            }

            String source = Files.readString(file);

            Matcher oracleMatcher = ORACLE_REFERENCE_PATTERN.matcher(source);
            while (oracleMatcher.find()) {
                String value = oracleMatcher.group();

                references.add(new OracleReference(
                        info.qualifiedName(),
                        info.path(),
                        value,
                        classifyOracleReference(value),
                        lineNumber(source, oracleMatcher.start()),
                        "REGEX_MATCH"
                ));
            }

            Matcher sqlMatcher = SQL_PATTERN.matcher(source);
            while (sqlMatcher.find()) {
                references.add(new OracleReference(
                        info.qualifiedName(),
                        info.path(),
                        sqlMatcher.group(),
                        "SQL_PATTERN",
                        lineNumber(source, sqlMatcher.start()),
                        "REGEX_MATCH"
                ));
            }
        }

        List<OracleReference> unique = references.stream()
                .distinct()
                .toList();

        List<OracleReference> returned = unique.stream()
                .limit(limit)
                .toList();

        return new OracleStaticReport(
                index.projectName(),
                visitedFiles.size(),
                unique.size(),
                returned.size(),
                unique.size() > limit,
                returned,
                "Regex-based source scan only. Comments and string literals may cause false positives. "
                        + "No Oracle connection was opened and no database metadata was queried."
        );
    }

    private ClassInfo findClass(ProjectIndex index, String className) {
        String name = safeQuery(className);

        return index.classes().values().stream()
                .filter(c -> c.qualifiedName().equals(name)
                        || c.name().equals(name))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Class not found: " + name));
    }

    private int normalizeLimit(int requestedLimit) {
        return Math.max(1, Math.min(requestedLimit, 500));
    }

    private String safeQuery(String value) {
        return value == null ? "" : value.trim();
    }

    private int lineNumber(String source, int offset) {
        int line = 1;

        for (int i = 0; i < offset && i < source.length(); i++) {
            if (source.charAt(i) == '\n') {
                line++;
            }
        }

        return line;
    }

    private String classifyOracleReference(String value) {
        String upper = value.toUpperCase(Locale.ROOT);

        if (upper.startsWith("DBMS_") || upper.startsWith("UTL_")) {
            return "ORACLE_BUILT_IN_PACKAGE";
        }

        if (upper.contains(".")) {
            return "POSSIBLE_SCHEMA_OR_PACKAGE_REFERENCE";
        }

        return "POSSIBLE_ORACLE_IDENTIFIER";
    }

    public record ProjectSummary(
            String projectName,
            int sourceFileCount,
            int parsedFileCount,
            int classCount,
            long methodCount,
            int methodCallCount,
            int parseErrorCount,
            String sourceDirectory,
            String note) {
    }

    public record ClassSearchResult(
            String qualifiedName,
            String name,
            String kind,
            String packageName,
            String sourcePath,
            int methodCount,
            int importCount,
            List<String> inheritance) {
    }

    public record ClassDetail(
            String projectName,
            String qualifiedName,
            String name,
            String kind,
            String packageName,
            String sourcePath,
            List<String> imports,
            List<String> inheritance,
            List<String> methods,
            int returnedCallCount,
            List<MethodCallInfo> calls) {
    }

    public record MethodSearchResult(
            String className,
            String simpleClassName,
            String methodDeclaration,
            String sourcePath) {
    }

    public record OracleReference(
            String sourceClass,
            String sourcePath,
            String matchedText,
            String referenceType,
            int line,
            String evidence) {
    }

    public record OracleStaticReport(
            String projectName,
            int filesScanned,
            int totalReferencesFound,
            int returnedReferences,
            boolean truncated,
            List<OracleReference> references,
            String note) {
    }
}
