
package spring.ai.example.spring_ai_demo.controller.v33;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.dto.v33.OracleJavaCascadeV33Response;
import spring.ai.example.spring_ai_demo.service.v33.OracleJavaCascadeV33Service;

import java.io.IOException;

@RestController
@RequestMapping("/api/v33/oracle-java-cascade")
public class OracleJavaCascadeV33Controller {

    private final OracleJavaCascadeV33Service service;

    public OracleJavaCascadeV33Controller(
            OracleJavaCascadeV33Service service) {
        this.service = service;
    }

    /**
     http://localhost:9999/api/v33/oracle-java-cascade?schema=CLSU_CANPEN&objectName=LSU_REQ&objectType=TABLE&maxDepth=3&utm_source=chatgpt.com


     در خروجی این فیلدها مهم‌اند:
     - oracleCascade: گراف وابستگی Oracle.
     - javaEvidence: تطبیق‌های Java برای هر شیء در گراف.
     - javaMatchCount: تعداد کل تطبیق‌های Java.
     - affectedClassCount: تعداد کلاس‌های Java یکتا.

     */
    @GetMapping
    public OracleJavaCascadeV33Response analyze(
            @RequestParam String schema,
            @RequestParam String objectName,
            @RequestParam String objectType,
            @RequestParam(defaultValue = "3") int maxDepth)
            throws IOException {

        return service.analyze(
                schema, objectName, objectType, maxDepth);
    }
}
