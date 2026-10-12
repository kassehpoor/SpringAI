package spring.ai.example.spring_ai_demo.service.v10.tool;

/**
 * @author Tahereh Kasehpoor
 */
public interface CoordinatorTool {

    ToolType getToolType();

    ToolResult execute(String input);
}