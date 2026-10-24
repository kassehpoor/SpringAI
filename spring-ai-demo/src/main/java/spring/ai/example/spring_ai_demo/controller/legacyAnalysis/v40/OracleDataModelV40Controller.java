package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v40;

import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v40.OracleTableModelV40DTO;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v40.OracleDataModelV40Service;

@RestController
@RequestMapping("/api/v40/data-model")
public class OracleDataModelV40Controller {

    private final OracleDataModelV40Service dataModelService;

    public OracleDataModelV40Controller(
            OracleDataModelV40Service dataModelService) {
        this.dataModelService = dataModelService;
    }

    /**

     خروجی شامل این ساختار خواهد بود:

     - columns: مشخصات ستون‌ها
     - constraints: کلیدهای اصلی، محدودیت‌ها و سایر قیود
     - foreignKeys: روابط خارجی تعریف‌شده برای جدول
     - status: وضعیت اولیه DRAFT
     */
    // http://localhost:9999/api/v40/data-model/table?schema=lsupcore0890&tableName=BAR_MALI_AFZAYESH
    @GetMapping("/table")
    public OracleTableModelV40DTO getTableModel(
            @RequestParam String schema,
            @RequestParam String tableName) {

        return dataModelService.getTableModel(schema, tableName);
    }
}