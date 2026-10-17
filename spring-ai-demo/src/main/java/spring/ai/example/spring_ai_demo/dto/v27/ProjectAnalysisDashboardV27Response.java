package spring.ai.example.spring_ai_demo.dto.v27;

public record ProjectAnalysisDashboardV27Response(
        String projectName,
        int javaClassCount,
        int javaMethodCallCount,
        String schema,
        int oracleObjectCount,
        String analysisMode,
        String message
) {
}