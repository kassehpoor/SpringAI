package spring.ai.example.spring_ai_demo.service.v14;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v14.OracleAnalysisResponse;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OracleStaticAnalysisService {

    private final ProjectStructureIndexService indexService;

    public OracleStaticAnalysisService(
            ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    private record Rule(String category, Pattern pattern) {}

    private static final List<Rule> RULES = List.of(
//
//            new Rule("SELECT_FROM", Pattern.compile(
//                    "\\bFROM\\s+([A-Za-z_$][\\w$#.]*)",
//                    Pattern.CASE_INSENSITIVE)),

            //نمونه مشخص service. را فیلتر می‌کند
            new Rule("SELECT_FROM", Pattern.compile(
                    "\\bFROM\\s+(?!service\\b|response\\b|request\\b|exception\\b)"
                            + "([A-Za-z_$][\\w$#]*(?:\\.[A-Za-z_$][\\w$#]*)?)"
                            + "(?=\\s|,|;|\\)|$)",
                    Pattern.CASE_INSENSITIVE)),
            new Rule("JOIN", Pattern.compile(
                    "\\bJOIN\\s+([A-Za-z_$][\\w$#.]*)",
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
                    "\\b(?:createNativeQuery|createStoredProcedureQuery)\\s*\\(",
                    Pattern.CASE_INSENSITIVE)),
            new Rule("JDBC_SQL", Pattern.compile(
                    "\\b(?:prepareStatement|prepareCall|executeQuery)\\s*\\(",
                    Pattern.CASE_INSENSITIVE))
    );

    public OracleAnalysisResponse analyze() {
        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "ابتدا ZIP را از طریق API مرحله V12 بارگذاری کن.");
        }

        List<OracleAnalysisResponse.Reference> references =
                new ArrayList<>();

        for (Map.Entry<String, String> entry :
                index.sourceFiles().entrySet()) {

            String path = entry.getKey();
            String source = entry.getValue();
            String[] lines = source.split("\\R", -1);

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];

                for (Rule rule : RULES) {
                    Matcher matcher = rule.pattern().matcher(line);

                    while (matcher.find()) {
                        String objectName = matcher.groupCount() >= 1
                                ? matcher.group(1)
                                : rule.category();

                        references.add(
                                new OracleAnalysisResponse.Reference(
                                        path,
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

        return new OracleAnalysisResponse(
                index.projectName(),
                index.sourceFiles().size(),
                references.size(),
                references.stream().limit(10_000).toList()
        );
    }
}

