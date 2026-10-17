


        package spring.ai.example.spring_ai_demo.service.v10;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.service.v10.tool.CoordinatorTool;
import spring.ai.example.spring_ai_demo.service.v10.tool.ToolResult;
import spring.ai.example.spring_ai_demo.service.v10.tool.ToolType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 این کلاس ابزارها را از Spring دریافت می‌کند، ابزار مناسب را انتخاب می‌کند و نتیجه را برمی‌گرداند.
 */
@Service
public class InsuranceCoordinatorService {

    private final Map<ToolType, CoordinatorTool> tools =
            new EnumMap<>(ToolType.class);

    public InsuranceCoordinatorService(List<CoordinatorTool> toolList) {
        for (CoordinatorTool tool : toolList) {
            tools.put(tool.getToolType(), tool);
        }
    }

    public ToolResult coordinate(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be blank"
            );
        }

        ToolType selectedTool = selectTool(question);

        CoordinatorTool tool = tools.get(selectedTool);

        if (tool == null) {
            return ToolResult.failure(
                    selectedTool,
                    "ابزار موردنیاز هنوز پیاده‌سازی یا فعال نشده است: "
                            + selectedTool
            );
        }

        return tool.execute(question.trim());
    }

    private ToolType selectTool(String question) {

        String normalized = question
                .replace('ي', 'ی')
                .replace('ك', 'ک')
                .toLowerCase();

        if (containsAny(normalized,
                "oracle",
                "اوراکل",
                "plsql",
                "sql",
                "جدول دیتابیس",
                "ساختار دیتابیس")) {

            return ToolType.ORACLE_ANALYSIS;
        }

        if (containsAny(normalized,
                "zip",
                "فایل فشرده",
                "تحلیل پروژه",
                "کدهای پروژه",
                "ساختار پروژه")) {

            return ToolType.PROJECT_ZIP_ANALYSIS;
        }

        if (containsAny(normalized,
                "وابستگی کلاس",
                "وابستگی متد",
                "کدام کلاس",
                "چه کلاس‌هایی",
                "فراخوانی متد",
                "dependency",
                "call hierarchy")) {

            return ToolType.CODE_DEPENDENCY_ANALYSIS;
        }

        return ToolType.INSURANCE_KNOWLEDGE;
    }

    private boolean containsAny(
            String text,
            String... keywords) {

        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}

