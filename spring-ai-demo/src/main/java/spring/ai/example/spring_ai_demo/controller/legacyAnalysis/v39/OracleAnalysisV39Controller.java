
package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v39;

import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.service.v22.OracleUsageAnalysisService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v39/oracle-analysis")
public class OracleAnalysisV39Controller {

    private final OracleUsageAnalysisService oracleService;

    public OracleAnalysisV39Controller(
            OracleUsageAnalysisService oracleService) {
        this.oracleService = oracleService;
    }

    /**
     شمارش اشیای Oracle و آمار ایندکس Java
     */
    @GetMapping("/summary")
    public Map<String, Object> summary(
            @RequestParam String schema) {
        return oracleService.summary(schema);
    }

    /**
     فهرست توابع
     */
    @GetMapping("/objects")
    public List<Map<String, Object>> objects(
            @RequestParam String schema,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name) {
        return oracleService.listOracleObjects(schema, type, name);
    }

    /**
     یافتن شواهد متنی استفاده در Java
     */
    @GetMapping("/usage")
    public Map<String, Object> usage(
            @RequestParam String schema,
            @RequestParam String objectName) throws IOException {
        return oracleService.findOracleUsage(schema, objectName);
    }

    /**
     اشیایی که نامشان در کد Java پیدا نشده است
     */
    @GetMapping("/unmatched")
    public List<Map<String, Object>> unmatched(
            @RequestParam String schema,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "100") int limit)
            throws IOException {
        return oracleService.findUnmatched(schema, type, limit);
    }
}
