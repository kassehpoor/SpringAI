
package spring.ai.example.spring_ai_demo.service.v34;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v34.OracleChangeImpactV34Response;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;
import spring.ai.example.spring_ai_demo.service.v25.JavaOracleLinkV25Service;

import java.io.IOException;
import java.util.*;

@Service
public class OracleChangeImpactV34Service {

    private final ProjectStructureIndexService indexService;
    private final OracleMetadataService metadataService;
    private final JavaOracleLinkV25Service javaLinkService;

    public OracleChangeImpactV34Service(
            ProjectStructureIndexService indexService,
            OracleMetadataService metadataService,
            JavaOracleLinkV25Service javaLinkService) {
        this.indexService = indexService;
        this.metadataService = metadataService;
        this.javaLinkService = javaLinkService;
    }

    public OracleChangeImpactV34Response analyze(
            String schema,
            String objectName,
            String objectType) throws IOException {

        var index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException("Project index is not ready.");
        }

        if (objectName != null && objectName.isBlank()) {
            objectName = null;
        }

        String resolvedSchema = schema == null || schema.isBlank()
                ? null : schema.trim().toUpperCase(Locale.ROOT);

        var oracleObjects = metadataService.listObjects(
                resolvedSchema, objectType, objectName);

        List<OracleChangeImpactV34Response.ObjectChange> results =
                new ArrayList<>();

        int matchedObjectCount = 0;

        for (Map<String, Object> row : oracleObjects) {
            String name = value(row, "OBJECT_NAME");
            String type = value(row, "OBJECT_TYPE");
            String status = value(row, "STATUS");
            String lastDdlTime = value(row, "LAST_DDL_TIME");

            if (name == null || type == null) {
                continue;
            }

            List<OracleChangeImpactV34Response.JavaMatch> matches =
                    new ArrayList<>();

            try {
                var javaResponse = javaLinkService.analyze(
                        resolvedSchema, name);

                matches = javaResponse.matches().stream()
                        .map(match ->
                                new OracleChangeImpactV34Response.JavaMatch(
                                        match.className(),
                                        match.methodName(),
                                        match.path(),
                                        match.line(),
                                        match.evidence()))
                        .toList();

            } catch (IOException ex) {
                throw ex;
            }

            int classCount = (int) matches.stream()
                    .map(OracleChangeImpactV34Response.JavaMatch::className)
                    .distinct()
                    .count();

            if (!matches.isEmpty()) {
                matchedObjectCount++;
            }

            results.add(
                    new OracleChangeImpactV34Response.ObjectChange(
                            name,
                            type,
                            status,
                            lastDdlTime,
                            matches.size(),
                            classCount,
                            matches));
        }

        return new OracleChangeImpactV34Response(
                index.projectName(),
                resolvedSchema == null ? "DEFAULT" : resolvedSchema,
                results.size(),
                matchedObjectCount,
                List.copyOf(results),
                "LAST_DDL_TIME indicates the last DDL timestamp, not the exact change. Java matches are textual evidence, not confirmed runtime dependencies."
        );
    }

    private String value(Map<String, Object> row, String key) {
        Object result = row.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        return result == null ? null : result.toString();
    }
}
