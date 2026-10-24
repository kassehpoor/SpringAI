package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v40;

import java.util.List;
import java.util.Map;

public record OracleSchemaModelV40DTO(
        String schema,
        List<Map<String, Object>> tables,
        List<Map<String, Object>> foreignKeys,
        int tableCount,
        String status
) {
}