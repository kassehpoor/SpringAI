package spring.ai.example.spring_ai_demo.dto.v12;



public record ProjectIndexResponse(
        String projectName,
        int javaFileCount,
        int parsedFileCount,
        int classCount,
        int methodCount,
        int methodCallCount,
        int failedFileCount,
        String message
) {
}