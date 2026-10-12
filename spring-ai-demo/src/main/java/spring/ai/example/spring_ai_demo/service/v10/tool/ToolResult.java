package spring.ai.example.spring_ai_demo.service.v10.tool;

/**
 * @author Tahereh Kasehpoor
 */


public record ToolResult(
        ToolType toolType,
        boolean success,
        String content,
        String errorMessage
) {

    public static ToolResult success(
            ToolType toolType,
            String content) {

        return new ToolResult(
                toolType,
                true,
                content,
                null
        );
    }

    public static ToolResult failure(
            ToolType toolType,
            String errorMessage) {

        return new ToolResult(
                toolType,
                false,
                null,
                errorMessage
        );
    }
}
