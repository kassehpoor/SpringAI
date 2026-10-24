package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v40;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v40.OracleSchemaModelV40DTO;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OracleSchemaModelV40Service {

    private final OracleMetadataService oracleMetadataService;

    public OracleSchemaModelV40Service(
            OracleMetadataService oracleMetadataService) {
        this.oracleMetadataService = oracleMetadataService;
    }

    public OracleSchemaModelV40DTO getSchemaModel(String schema) {

        if (schema == null || schema.isBlank()) {
            throw new IllegalArgumentException("Schema is required");
        }

        String normalizedSchema = schema.trim().toUpperCase(Locale.ROOT);

        List<Map<String, Object>> objects =
                oracleMetadataService.listObjects(
                        normalizedSchema, "TABLE", null);

        List<Map<String, Object>> tables = objects.stream()
                .map(object -> {
                    Object name = object.get("OBJECT_NAME");

                    if (name == null) {
                        name = object.get("object_name");
                    }

                    String tableName = String.valueOf(name);

                    List<Map<String, Object>> columns =
                            oracleMetadataService.listColumns(
                                    normalizedSchema, tableName);

                    List<Map<String, Object>> constraints =
                            oracleMetadataService.listConstraints(
                                    normalizedSchema, tableName);

                    return Map.<String, Object>of(
                            "tableName", tableName,
                            "columns", columns,
                            "constraints", constraints
                    );
                })
                .toList();

        List<Map<String, Object>> foreignKeys =
                oracleMetadataService.listForeignKeys(normalizedSchema);

        return new OracleSchemaModelV40DTO(
                normalizedSchema,
                tables,
                foreignKeys,
                tables.size(),
                "DRAFT"
        );
    }
}