package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v41;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v41.OracleErdV41DTO;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OracleErdV41Service {

    private final OracleMetadataService oracleMetadataService;

    public OracleErdV41Service(OracleMetadataService oracleMetadataService) {
        this.oracleMetadataService = oracleMetadataService;
    }

    public OracleErdV41DTO generate(String schema) {
        if (schema == null || schema.isBlank()) {
            throw new IllegalArgumentException("Schema is required");
        }

        String owner = schema.trim().toUpperCase(Locale.ROOT);

        List<Map<String, Object>> objects =
                oracleMetadataService.listObjects(owner, "TABLE", null);

        List<String> tables = objects.stream()
                .map(row -> value(row, "OBJECT_NAME"))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .distinct()
                .sorted()
                .toList();

        List<Map<String, Object>> foreignKeys =
                oracleMetadataService.listForeignKeys(owner);

        StringBuilder mermaid = new StringBuilder("erDiagram\n");

        // ساخت موجودیت‌ها و ستون‌ها
        for (String table : tables) {
            mermaid.append("    ")
                    .append(identifier(table))
                    .append(" {\n");

            List<Map<String, Object>> columns =
                    oracleMetadataService.listColumns(owner, table);

            List<Map<String, Object>> constraints =
                    oracleMetadataService.listConstraints(owner, table);

            Set<String> primaryKeyColumns = constraints.stream()
                    .filter(row -> "P".equalsIgnoreCase(
                            value(row, "CONSTRAINT_TYPE")))
                    .map(row -> value(row, "COLUMN_NAME"))
                    .filter(Objects::nonNull)
                    .map(name -> name.toUpperCase(Locale.ROOT))
                    .collect(Collectors.toSet());

            for (Map<String, Object> column : columns) {
                String columnName = value(column, "COLUMN_NAME");
                String dataType = value(column, "DATA_TYPE");

                if (columnName == null) {
                    continue;
                }

                String type = identifier(
                        dataType == null ? "UNKNOWN" : dataType);

                String key = primaryKeyColumns.contains(
                        columnName.toUpperCase(Locale.ROOT))
                        ? " PK"
                        : "";

                mermaid.append("        ")
                        .append(type)
                        .append(" ")
                        .append(identifier(columnName))
                        .append(key)
                        .append("\n");
            }

            mermaid.append("    }\n");
        }

        // فقط روابطی که هر دو جدول در همین Schema وجود دارند
        int relationshipCount = 0;

        Set<String> tableSet = tables.stream()
                .map(name -> name.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        for (Map<String, Object> fk : foreignKeys) {
            String child = value(fk, "TABLE_NAME");
            String parent = value(fk, "REFERENCED_TABLE");
            String constraintName = value(fk, "CONSTRAINT_NAME");

            if (child == null || parent == null) {
                continue;
            }

            if (!tableSet.contains(child.toUpperCase(Locale.ROOT))
                    || !tableSet.contains(parent.toUpperCase(Locale.ROOT))) {
                continue;
            }

            mermaid.append("    ")
                    .append(identifier(parent))
                    .append(" ||--o{ ")
                    .append(identifier(child))
                    .append(" : ")
                    .append("\"")
                    .append(identifier(
                            constraintName == null ? "FK" : constraintName))
                    .append("\"\n");

            relationshipCount++;
        }

        return new OracleErdV41DTO(
                owner,
                mermaid.toString(),
                tables.size(),
                relationshipCount,
                "DRAFT"
        );
    }

    private String value(Map<String, Object> row, String key) {
        if (row == null) {
            return null;
        }

        Object result = row.get(key);

        if (result == null) {
            result = row.get(key.toLowerCase(Locale.ROOT));
        }

        return result == null ? null : result.toString();
    }

    private String identifier(String input) {
        String result = input.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9_]", "_");

        if (result.isBlank()) {
            return "UNKNOWN";
        }

        if (Character.isDigit(result.charAt(0))) {
            return "N_" + result;
        }

        return result;
    }
}