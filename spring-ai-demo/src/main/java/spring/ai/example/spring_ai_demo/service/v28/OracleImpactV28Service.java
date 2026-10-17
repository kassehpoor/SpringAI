
package spring.ai.example.spring_ai_demo.service.v28;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v25.JavaOracleLinkV25Response;
import spring.ai.example.spring_ai_demo.dto.v28.OracleImpactV28Response;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;
import spring.ai.example.spring_ai_demo.service.v25.JavaOracleLinkV25Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Service
public class OracleImpactV28Service {

    private final ProjectStructureIndexService indexService;
    private final OracleMetadataService oracleMetadataService;
    private final JavaOracleLinkV25Service javaOracleLinkService;

    public OracleImpactV28Service(
            ProjectStructureIndexService indexService,
            OracleMetadataService oracleMetadataService,
            JavaOracleLinkV25Service javaOracleLinkService) {
        this.indexService = indexService;
        this.oracleMetadataService = oracleMetadataService;
        this.javaOracleLinkService = javaOracleLinkService;
    }

    public OracleImpactV28Response analyze(
            String schema,
            String objectName) throws IOException {

        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException(
                    "Oracle object name is required.");
        }

        var index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "Project index is not ready.");
        }

        String normalizedName = objectName.trim()
                .toUpperCase(Locale.ROOT);

        String resolvedSchema =
                schema == null || schema.isBlank()
                        ? "DEFAULT"
                        : schema.trim().toUpperCase(Locale.ROOT);

        // بررسی وجود شیء در متادیتای Oracle
        var oracleObjects = oracleMetadataService.listObjects(
                schema, null, normalizedName);

        // استفاده از متد واقعی موجود در V25
        JavaOracleLinkV25Response v25Response =
                javaOracleLinkService.analyze(
                        schema, normalizedName);

        // تبدیل نتایج V25 به DTO نسخه V28
        List<OracleImpactV28Response.JavaMatch> javaMatches =
                v25Response.matches().stream()
                        .map(match ->
                                new OracleImpactV28Response.JavaMatch(
                                        match.className(),
                                        match.methodName(),
                                        match.path(),
                                        match.line(),
                                        match.evidence()))
                        .toList();

        String warning = oracleObjects.isEmpty()
                ? "The Oracle object was not found in metadata. Java matches may still exist."
                : "Java matches are based on static text search, not confirmed runtime dependencies.";

        return new OracleImpactV28Response(
                index.projectName(),
                resolvedSchema,
                normalizedName,
                javaMatches.size(),
                javaMatches,
                "STATIC_SOURCE_ANALYSIS",
                warning
        );
    }
}
