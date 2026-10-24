package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v40;

import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v40/oracle-metadata")
public class OracleMetadataV40Controller {

    private final OracleMetadataService oracleMetadataService;

    public OracleMetadataV40Controller(
            OracleMetadataService oracleMetadataService) {
        this.oracleMetadataService = oracleMetadataService;
    }

    @GetMapping("/columns")
    public List<Map<String, Object>> columns(
            @RequestParam String schema,
            @RequestParam String tableName) {
        return oracleMetadataService.listColumns(schema, tableName);
    }

    @GetMapping("/constraints")
    public List<Map<String, Object>> constraints(
            @RequestParam String schema,
            @RequestParam String tableName) {
        return oracleMetadataService.listConstraints(schema, tableName);
    }

    // http://localhost:9999/api/v40/oracle-metadata/table?schema=lsupcore0890&tableName=BAR_MALI_AFZAYESH
    @GetMapping("/table")
    public Map<String, Object> tableMetadata(
            @RequestParam String schema,
            @RequestParam String tableName) {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("schema", schema);
        result.put("tableName", tableName);
        result.put(
                "columns",
                oracleMetadataService.listColumns(schema, tableName)
        );
        result.put(
                "constraints",
                oracleMetadataService.listConstraints(schema, tableName)
        );

        return result;
    }

 // http://localhost:9999/api/v40/oracle-metadata/foreign-keys?schema=lsupcore0890

// یک endpoint  که تمام Foreign Keyهای یک Schema را استخراج کند. این روابط پایه‌ی طراحی ERD و مدل داده هستند.
    @GetMapping("/foreign-keys")
    public List<Map<String, Object>> foreignKeys(
            @RequestParam String schema) {

        return oracleMetadataService.listForeignKeys(schema);
    }
}