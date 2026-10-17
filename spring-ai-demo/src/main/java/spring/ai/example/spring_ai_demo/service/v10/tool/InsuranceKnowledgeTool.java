package spring.ai.example.spring_ai_demo.service.v10.tool;



import org.springframework.stereotype.Component;
import spring.ai.example.spring_ai_demo.service.v9.InsuranceAgentService;

@Component
public class InsuranceKnowledgeTool implements CoordinatorTool {

    private final InsuranceAgentService insuranceAgentService;

    public InsuranceKnowledgeTool(
            InsuranceAgentService insuranceAgentService) {

        this.insuranceAgentService = insuranceAgentService;
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSURANCE_KNOWLEDGE;
    }

    @Override
    public ToolResult execute(String input) {

        if (input == null || input.isBlank()) {
            return ToolResult.failure(
                    getToolType(),
                    "ورودی ابزار دانش بیمه نمی‌تواند خالی باشد."
            );
        }

        try {
            String answer = insuranceAgentService.ask(input);

            return ToolResult.success(
                    getToolType(),
                    answer
            );

        } catch (IllegalArgumentException exception) {
            return ToolResult.failure(
                    getToolType(),
                    exception.getMessage()
            );
        }
    }
}
