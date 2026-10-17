package spring.ai.example.spring_ai_demo.service.v25;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v25.JavaOracleLinkV25Response;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class JavaOracleLinkV25Service {

    private final ProjectStructureIndexService indexService;
    private final OracleMetadataService oracleMetadataService;

    public JavaOracleLinkV25Service(
            ProjectStructureIndexService indexService,
            OracleMetadataService oracleMetadataService) {
        this.indexService = indexService;
        this.oracleMetadataService = oracleMetadataService;
    }

    public JavaOracleLinkV25Response analyze(
            String schema,
            String objectName) throws IOException {

        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException("Project index is not ready.");
        }

        String normalizedName = objectName.trim()
                .toUpperCase(Locale.ROOT);

        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException(
                    "Oracle object name is required.");
        }

        List<JavaOracleLinkV25Response.Match> matches =
                new ArrayList<>();

        for (ClassInfo classInfo : index.classes().values()) {
            Path file = index.sourceDirectory()
                    .resolve(classInfo.path())
                    .normalize();

            if (!file.startsWith(index.sourceDirectory().normalize())
                    || !Files.isRegularFile(file)) {
                continue;
            }

            List<String> lines = Files.readAllLines(file);
            StringBuilder evidence = new StringBuilder();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.toUpperCase(Locale.ROOT)
                        .contains(normalizedName)) {
                    if (!evidence.isEmpty()) {
                        evidence.append(" | ");
                    }

                    evidence.append(line.trim());

                    matches.add(
                            new JavaOracleLinkV25Response.Match(
                                    classInfo.qualifiedName(),
                                    findMethodName(index, classInfo, i + 1),
                                    classInfo.path(),
                                    i + 1,
                                    line.trim()
                            )
                    );
                }
            }
        }

        String resolvedSchema = schema == null || schema.isBlank()
                ? "DEFAULT"
                : schema.trim().toUpperCase(Locale.ROOT);

        return new JavaOracleLinkV25Response(
                index.projectName(),
                resolvedSchema,
                normalizedName,
                matches.stream()
                        .map(JavaOracleLinkV25Response.Match::path)
                        .distinct()
                        .toList()
                        .size(),
                matches
        );
    }

    private String findMethodName(
            ProjectIndex index,
            ClassInfo classInfo,
            int line) {

        return index.calls().stream()
                .filter(call -> call.path().equals(classInfo.path()))
                .filter(call -> call.line() <= line)
                .max(java.util.Comparator.comparingInt(
                        ProjectStructureIndexService.MethodCallInfo::line))
                .map(ProjectStructureIndexService.MethodCallInfo::sourceMethod)
                .orElse("unknown");
    }
}