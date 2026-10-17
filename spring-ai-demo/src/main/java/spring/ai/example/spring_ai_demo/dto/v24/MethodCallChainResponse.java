package spring.ai.example.spring_ai_demo.dto.v24;



import java.util.List;

public record MethodCallChainResponse(
        String projectName,
        String sourceClass,
        String sourceMethod,
        List<CallStep> chain,
        int totalCalls
) {
    public record CallStep(
            String className,
            String methodName,
            String path,
            int line,
            String scope
    ) {
    }
}
