package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v45;

import java.util.List;

public record GeneratedCodeV45DTO(
        String projectName,
        String packageName,
        List<JavaFile> files,
        List<String> assumptions,
        List<String> reviewNotes
) {
    public record JavaFile(
            String path,
            String fileName,
            String type,
            String content
    ) {}
}