package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v40;

import java.util.List;
import java.util.Map;

public record OracleTableModelV40DTO(
        String schema,
        String tableName,
        List<Map<String, Object>> columns,
        List<Map<String, Object>> constraints,
        List<Map<String, Object>> foreignKeys,
        String status
) {
}