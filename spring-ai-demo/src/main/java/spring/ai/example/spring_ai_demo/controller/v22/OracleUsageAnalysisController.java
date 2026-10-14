package spring.ai.example.spring_ai_demo.controller.v22;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.service.v22.OracleUsageAnalysisService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v22/analysis")
@ConditionalOnProperty(
        prefix = "app.oracle",
        name = "enabled",
        havingValue = "true"
)
public class OracleUsageAnalysisController {

    private final OracleUsageAnalysisService analysisService;

    public OracleUsageAnalysisController(
            OracleUsageAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

//    خلاصه تحلیل
//    تعداد کلاس‌ها، فراخوانی‌های Java و اشیای Oracle قابل‌مشاهده را نشان می‌دهد.
    @GetMapping("/summary")
    public Map<String, Object> summary(
            @RequestParam(required = false) String schema) {
        return analysisService.summary(schema);
    }

//    فهرست اشیای Oracle
//    برای دیدن Functionها یا Procedureها، مقدار type را تغییر بده.
    @GetMapping("/oracle-objects")
    public List<Map<String, Object>> oracleObjects(
            @RequestParam(required = false) String schema,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name) {

        return analysisService.listOracleObjects(schema, type, name);
    }

//جست‌وجوی استفاده در Java
//PENSION_PKG را با نام واقعی یکی از اشیای دیتابیس جایگزین کن.
    @GetMapping("/oracle-usage")
    public Map<String, Object> oracleUsage(
            @RequestParam(required = false) String schema,
            @RequestParam String name) throws IOException {

        return analysisService.findOracleUsage(schema, name);
    }

// اشیای بدون تطبیق متنی
// اشیایی را فهرست می‌کند که نامشان در فایل‌های Java پیدا نشده است؛ این نتیجه به‌تنهایی به
//معنی بلااستفاده بودن آن‌ها نیست.

    @GetMapping("/unmatched")
    public List<Map<String, Object>> unmatched(
            @RequestParam(required = false) String schema,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "50") int limit) throws IOException {

        return analysisService.findUnmatched(schema, type, limit);
    }
}
