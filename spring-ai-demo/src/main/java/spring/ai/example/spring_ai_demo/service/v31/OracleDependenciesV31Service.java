
package spring.ai.example.spring_ai_demo.service.v31;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v31.OracleDependenciesV31Response;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

import java.util.Locale;

@Service
public class OracleDependenciesV31Service {

    private final OracleMetadataService oracleMetadataService;

    public OracleDependenciesV31Service(
            OracleMetadataService oracleMetadataService) {
        this.oracleMetadataService = oracleMetadataService;
    }

    public OracleDependenciesV31Response analyze(
            String schema,
            String objectName,
            String objectType) {

        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException(
                    "objectName is required.");
        }

        if (objectType == null || objectType.isBlank()) {
            throw new IllegalArgumentException(
                    "objectType is required.");
        }

        String normalizedSchema = schema == null || schema.isBlank()
                ? null
                : schema.trim().toUpperCase(Locale.ROOT);

        String normalizedName =
                objectName.trim().toUpperCase(Locale.ROOT);

        String normalizedType =
                objectType.trim().toUpperCase(Locale.ROOT);

        var dependencies = oracleMetadataService.listDependencies(
                normalizedSchema,
                normalizedName,
                normalizedType);

        return new OracleDependenciesV31Response(
                normalizedSchema == null ? "DEFAULT" : normalizedSchema,
                normalizedName,
                normalizedType,
                dependencies.size(),
                dependencies
        );
    }
}
