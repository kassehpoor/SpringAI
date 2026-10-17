
package spring.ai.example.spring_ai_demo.service.v29;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v25.JavaOracleLinkV25Response;
import spring.ai.example.spring_ai_demo.dto.v29.OracleImpactAnalysisV29Response;
import spring.ai.example.spring_ai_demo.service.v25.JavaOracleLinkV25Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Service
public class OracleImpactAnalysisV29Service {

    private final JavaOracleLinkV25Service javaOracleLinkService;

    public OracleImpactAnalysisV29Service(
            JavaOracleLinkV25Service javaOracleLinkService) {
        this.javaOracleLinkService = javaOracleLinkService;
    }

    public OracleImpactAnalysisV29Response analyze(
            String schema,
            String objectName) throws IOException {

        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException(
                    "Oracle object name is required.");
        }

        String normalizedName = objectName.trim()
                .toUpperCase(Locale.ROOT);

        JavaOracleLinkV25Response response =
                javaOracleLinkService.analyze(schema, normalizedName);

        List<OracleImpactAnalysisV29Response.AffectedJavaFile> affectedFiles =
                response.matches().stream()
                        .map(match ->
                                new OracleImpactAnalysisV29Response.AffectedJavaFile(
                                        match.className(),
                                        match.methodName(),
                                        match.path(),
                                        match.line(),
                                        match.evidence()))
                        .toList();

        int affectedClassCount = (int) affectedFiles.stream()
                .map(OracleImpactAnalysisV29Response.AffectedJavaFile::className)
                .distinct()
                .count();

        return new OracleImpactAnalysisV29Response(
                response.projectName(),
                response.schema(),
                normalizedName,
                affectedFiles.size(),
                affectedClassCount,
                affectedFiles,
                "STATIC_SOURCE_ANALYSIS",
                "Results are based on textual matches in Java source. Review each match before changing the Oracle object."
        );
    }
}
