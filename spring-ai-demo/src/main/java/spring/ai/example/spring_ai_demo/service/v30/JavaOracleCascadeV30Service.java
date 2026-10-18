
package spring.ai.example.spring_ai_demo.service.v30;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v30.JavaOracleCascadeV30Response;
import spring.ai.example.spring_ai_demo.service.v25.JavaOracleLinkV25Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class JavaOracleCascadeV30Service {

    private final JavaOracleLinkV25Service javaOracleLinkService;

    public JavaOracleCascadeV30Service(
            JavaOracleLinkV25Service javaOracleLinkService) {
        this.javaOracleLinkService = javaOracleLinkService;
    }

    public JavaOracleCascadeV30Response analyze(
            String schema,
            String objectNames) throws IOException {

        if (objectNames == null || objectNames.isBlank()) {
            throw new IllegalArgumentException(
                    "At least one Oracle object name is required.");
        }

        List<String> names = java.util.Arrays.stream(
                        objectNames.split(","))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .map(name -> name.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();

        if (names.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one valid Oracle object name is required.");
        }

        List<JavaOracleCascadeV30Response.ObjectImpact> impacts =
                new ArrayList<>();

        java.util.Set<String> affectedClasses =
                new java.util.HashSet<>();

        int totalMatches = 0;
        String projectName = null;
        String resolvedSchema = schema == null || schema.isBlank()
                ? "DEFAULT"
                : schema.trim().toUpperCase(Locale.ROOT);

        for (String objectName : names) {
            var response = javaOracleLinkService.analyze(
                    schema, objectName);

            projectName = response.projectName();
            resolvedSchema = response.schema();

            List<JavaOracleCascadeV30Response.JavaMatch> matches =
                    response.matches().stream()
                            .map(match ->
                                    new JavaOracleCascadeV30Response.JavaMatch(
                                            match.className(),
                                            match.methodName(),
                                            match.path(),
                                            match.line(),
                                            match.evidence()))
                            .toList();

            matches.stream()
                    .map(JavaOracleCascadeV30Response.JavaMatch::className)
                    .forEach(affectedClasses::add);

            totalMatches += matches.size();

            impacts.add(
                    new JavaOracleCascadeV30Response.ObjectImpact(
                            objectName,
                            matches.size(),
                            matches));
        }

        return new JavaOracleCascadeV30Response(
                projectName,
                resolvedSchema,
                names.size(),
                totalMatches,
                affectedClasses.size(),
                impacts,
                "STATIC_SOURCE_ANALYSIS",
                "Results are textual Java-source matches. They do not prove runtime dependencies or transitive Oracle dependencies."
        );
    }
}
