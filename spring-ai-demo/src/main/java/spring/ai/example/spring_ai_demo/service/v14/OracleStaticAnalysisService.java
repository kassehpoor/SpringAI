
package spring.ai.example.spring_ai_demo.service.v14;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v14.OracleAnalysisResponse;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class OracleStaticAnalysisService {

    private final ProjectStructureIndexService indexService;

    public OracleStaticAnalysisService(
            ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    private record Rule(String category, Pattern pattern) {}

    private static final List<Rule> RULES = List.of(
            new Rule("SELECT_FROM", Pattern.compile(
                    "\\bFROM\\s+([A-Za-z_$][\\w$#]*"
                            + "(?:\\.[A-Za-z_$][\\w$#]*)?)"
                            + "(?=\\s|,|;|\\)|$)",
                    Pattern.CASE_INSENSITIVE)),

            new Rule("JOIN", Pattern.compile(
                    "\\bJOIN\\s+([A-Za-z_$][\\w$#]*"
                            + "(?:\\.[A-Za-z_$][\\w$#]*)?)",
                    Pattern.CASE_INSENSITIVE)),

            new Rule("INSERT_INTO", Pattern.compile(
                    "\\bINSERT\\s+INTO\\s+([A-Za-z_$][\\w$#.]*)",
                    Pattern.CASE_INSENSITIVE)),

            new Rule("UPDATE", Pattern.compile(
                    "\\bUPDATE\\s+([A-Za-z_$][\\w$#.]*)",
                    Pattern.CASE_INSENSITIVE)),

            new Rule("DELETE_FROM", Pattern.compile(
                    "\\bDELETE\\s+FROM\\s+([A-Za-z_$][\\w$#.]*)",
                    Pattern.CASE_INSENSITIVE)),

            new Rule("CALL", Pattern.compile(
                    "\\bCALL\\s+([A-Za-z_$][\\w$#.]*)",
                    Pattern.CASE_INSENSITIVE)),

            new Rule("TABLE_ANNOTATION", Pattern.compile(
                    "@Table\\s*\\([^)]*?name\\s*=\\s*\"([^\"]+)\"",
                    Pattern.CASE_INSENSITIVE)),

            new Rule("ENTITY_MANAGER", Pattern.compile(
                    "\\b(?:createNativeQuery|createStoredProcedureQuery)"
                            + "\\s*\\(",
                    Pattern.CASE_INSENSITIVE)),

            new Rule("JDBC_SQL", Pattern.compile(
                    "\\b(?:prepareStatement|prepareCall|executeQuery)"
                            + "\\s*\\(",
                    Pattern.CASE_INSENSITIVE))
    );

    public OracleAnalysisResponse analyze() {
        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "نمایه پروژه آماده نیست؛ ZIP را از طریق V12 بارگذاری کن."
            );
        }

        Path sourceDirectory = index.sourceDirectory();

        if (!Files.isDirectory(sourceDirectory)) {
            throw new IllegalStateException(
                    "پوشه سورس پروژه پیدا نشد: " + sourceDirectory
            );
        }

        List<OracleAnalysisResponse.Reference> references =
                new ArrayList<>();

        int totalFilesScanned = 0;

        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            List<Path> javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".java"))
                    .sorted()
                    .toList();

            totalFilesScanned = javaFiles.size();

            for (Path javaFile : javaFiles) {
                String relativePath = sourceDirectory
                        .relativize(javaFile)
                        .toString()
                        .replace('\\', '/');

                List<String> lines = Files.readAllLines(
                        javaFile,
                        StandardCharsets.UTF_8
                );

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);

                    for (Rule rule : RULES) {
                        Matcher matcher = rule.pattern().matcher(line);

                        while (matcher.find()) {
                            String objectName =
                                    matcher.groupCount() >= 1
                                            ? matcher.group(1)
                                            : rule.category();

                            references.add(
                                    new OracleAnalysisResponse.Reference(
                                            relativePath,
                                            i + 1,
                                            rule.category(),
                                            objectName,
                                            line.trim()
                                    )
                            );
                        }
                    }
                }
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "خواندن سورس‌های ذخیره‌شده با خطا مواجه شد.",
                    exception
            );
        }

        int totalReferencesFound = references.size();

        return new OracleAnalysisResponse(
                index.projectName(),
                totalFilesScanned,
                totalReferencesFound,
                references.stream().limit(10_000).toList()
        );
    }
}
