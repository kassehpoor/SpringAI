package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v40;

import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v40.OracleSchemaModelV40DTO;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v40.OracleSchemaModelV40Service;

@RestController
@RequestMapping("/api/v40/schema-model")
public class OracleSchemaModelV40Controller {

    private final OracleSchemaModelV40Service schemaModelService;

    public OracleSchemaModelV40Controller(
            OracleSchemaModelV40Service schemaModelService) {
        this.schemaModelService = schemaModelService;
    }

    // خروجی شامل نام جدول‌ها، ستون‌ها، محدودیت‌ها و کلیدهای خارجی خواهد بود.
    @GetMapping
    public OracleSchemaModelV40DTO getSchemaModel(
            @RequestParam String schema) {
        return schemaModelService.getSchemaModel(schema);
    }
}