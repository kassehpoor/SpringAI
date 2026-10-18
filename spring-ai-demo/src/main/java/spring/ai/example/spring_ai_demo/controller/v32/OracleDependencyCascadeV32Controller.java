
package spring.ai.example.spring_ai_demo.controller.v32;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.dto.v32.OracleDependencyCascadeV32Response;
import spring.ai.example.spring_ai_demo.service.v32.OracleDependencyCascadeV32Service;

@RestController
@RequestMapping("/api/v32/oracle-dependency-cascade")
public class OracleDependencyCascadeV32Controller {

    private final OracleDependencyCascadeV32Service service;

    public OracleDependencyCascadeV32Controller(
            OracleDependencyCascadeV32Service service) {
        this.service = service;
    }


    /**
     http://localhost:9999/api/v32/oracle-dependency-cascade?schema=CLSU_CANPEN&objectName=LSU_REQ&objectType=TABLE&maxDepth=3&utm_source=chatgpt.com


     تفسیر خروجی:
     - objects: اشیای یکتای دیده‌شده، شامل شیء ریشه.
     - dependencies: یال‌های وابستگی که از ALL_DEPENDENCIES آمده‌اند.
     - maxDepth: حداکثر تعداد سطوحی که پیمایش می‌کنیم.
     */
    @GetMapping
    public OracleDependencyCascadeV32Response analyze(
            @RequestParam String schema,
            @RequestParam String objectName,
            @RequestParam String objectType,
            @RequestParam(defaultValue = "3") int maxDepth) {

        return service.analyze(
                schema, objectName, objectType, maxDepth);
    }
}
