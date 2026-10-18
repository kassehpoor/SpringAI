
package spring.ai.example.spring_ai_demo.dto.v31;

import java.util.List;
import java.util.Map;

public record OracleDependenciesV31Response(
        String schema,
        String objectName,
        String objectType,
        int dependencyCount,
        List<Map<String, Object>> dependencies
) {}
