
package spring.ai.example.spring_ai_demo.controller.v28;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.dto.v28.OracleImpactV28Response;
import spring.ai.example.spring_ai_demo.service.v28.OracleImpactV28Service;

import java.io.IOException;

@RestController
@RequestMapping("/api/v28/oracle-impact")
public class OracleImpactV28Controller {

    private final OracleImpactV28Service service;

    public OracleImpactV28Controller(
            OracleImpactV28Service service) {
        this.service = service;
    }

    @GetMapping
    public OracleImpactV28Response analyze(
            @RequestParam String objectName,
            @RequestParam(required = false) String schema)
            throws IOException {

        return service.analyze(schema, objectName);
    }
}
