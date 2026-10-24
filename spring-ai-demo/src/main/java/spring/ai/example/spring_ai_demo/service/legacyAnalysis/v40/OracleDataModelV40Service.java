package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v40;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v40.OracleTableModelV40DTO;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OracleDataModelV40Service {

    private final OracleMetadataService oracleMetadataService;

    public OracleDataModelV40Service(
            OracleMetadataService oracleMetadataService) {
        this.oracleMetadataService = oracleMetadataService;
    }

    public OracleTableModelV40DTO getTableModel(
            String schema,
            String tableName) {

        List<Map<String, Object>> columns =
                oracleMetadataService.listColumns(schema, tableName);

        if (columns.isEmpty()) {
            throw new IllegalArgumentException(
                    "Table not found or has no visible columns: "
                            + tableName
            );
        }

        List<Map<String, Object>> constraints =
                oracleMetadataService.listConstraints(schema, tableName);

        List<Map<String, Object>> foreignKeys =
                oracleMetadataService.listForeignKeys(schema)
                        .stream()
                        .filter(row -> tableName.equalsIgnoreCase(
                                String.valueOf(row.get("TABLE_NAME"))))
                        .collect(Collectors.toList());

        return new OracleTableModelV40DTO(
                schema.toUpperCase(),
                tableName.toUpperCase(),
                columns,
                constraints,
                foreignKeys,
                "DRAFT"
        );
    }
}