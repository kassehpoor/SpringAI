package spring.ai.example.spring_ai_demo.dto.v10;

import spring.ai.example.spring_ai_demo.service.v10.tool.ToolType;


/**
 * این پاسخ به ما اجازه می‌دهد علاوه بر متن، ببینیم کدام ابزار انتخاب شده و اجرای آن موفق بوده است یا خیر.
 * @param question
 * @param selectedTool
 * @param success
 * @param answer
 * @param errorMessage
 */
public record CoordinatorResponse(
        String question,
        ToolType selectedTool,
        boolean success,
        String answer,
        String errorMessage
) {
}
