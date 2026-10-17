package spring.ai.example.spring_ai_demo.dto.v11;



import java.util.List;

public record ProjectAnalysisResponse(
        String projectName,
        int javaFileCount,
        int parsedFileCount,
        int failedFileCount,
        List<JavaFileSummary> files,
        List<String> errors
) {

    public record JavaFileSummary(
            String path,
            String packageName,
            List<String> imports,
            List<TypeSummary> types
    ) {
    }

    public record TypeSummary(
            String name,
            String kind,
            List<String> methods
    ) {
    }
}