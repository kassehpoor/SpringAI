
package spring.ai.example.spring_ai_demo.controller.v36;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.dto.v36.OracleImpactReportV36Response;
import spring.ai.example.spring_ai_demo.service.v36.OracleImpactReportV36Service;

import java.io.IOException;

@RestController
@RequestMapping("/api/v36/oracle-impact-report")
public class OracleImpactReportV36Controller {

    private final OracleImpactReportV36Service service;

    public OracleImpactReportV36Controller(
            OracleImpactReportV36Service service) {
        this.service = service;
    }

    /**
     http://localhost:9999/api/v36/oracle-impact-report?schema=CLSU_CANPEN&objectName=LSU_REQ&objectType=TABLE&maxDepth=3&utm_source=chatgpt.com


     در پاسخ، مهم‌ترین فیلدها عبارت‌اند از:
     - items: اشیای Oracle در زنجیره و تطبیق‌های Java هر شیء.
     - depth: فاصله هر شیء از شیء ریشه در گراف وابستگی.
     - reviewPriority: اولویت پیشنهادی برای بررسی.
     - affectedClassCount: تعداد کلاس‌های یکتای دارای تطبیق.
     */
    @GetMapping
    public OracleImpactReportV36Response analyze(
            @RequestParam String schema,
            @RequestParam String objectName,
            @RequestParam String objectType,
            @RequestParam(defaultValue = "3") int maxDepth)
            throws IOException {

        return service.analyze(
                schema, objectName, objectType, maxDepth);
    }
}
