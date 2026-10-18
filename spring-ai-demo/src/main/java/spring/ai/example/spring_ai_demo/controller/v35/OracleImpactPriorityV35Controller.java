
package spring.ai.example.spring_ai_demo.controller.v35;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.dto.v35.OracleImpactPriorityV35Response;
import spring.ai.example.spring_ai_demo.service.v35.OracleImpactPriorityV35Service;

import java.io.IOException;

@RestController
@RequestMapping("/api/v35/oracle-impact-priority")
public class OracleImpactPriorityV35Controller {

    private final OracleImpactPriorityV35Service service;

    public OracleImpactPriorityV35Controller(
            OracleImpactPriorityV35Service service) {
        this.service = service;
    }

    /**
     http://localhost:9999/api/v35/oracle-impact-priority?schema=CLSU_CANPEN&objectName=LSU_REQ&objectType=TABLE&utm_source=chatgpt.com




     */
    @GetMapping
    public OracleImpactPriorityV35Response analyze(
            @RequestParam(required = false) String schema,
            @RequestParam(required = false) String objectName,
            @RequestParam(required = false) String objectType)
            throws IOException {

        return service.analyze(schema, objectName, objectType);
    }
}
