
package spring.ai.example.spring_ai_demo.controller.v29;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.dto.v29.OracleImpactAnalysisV29Response;
import spring.ai.example.spring_ai_demo.service.v29.OracleImpactAnalysisV29Service;

import java.io.IOException;

@RestController
@RequestMapping("/api/v29/oracle-impact-analysis")
public class OracleImpactAnalysisV29Controller {

    private final OracleImpactAnalysisV29Service service;

    public OracleImpactAnalysisV29Controller(
            OracleImpactAnalysisV29Service service) {
        this.service = service;
    }

    @GetMapping
    public OracleImpactAnalysisV29Response analyze(
            @RequestParam String objectName,
            @RequestParam(required = false) String schema)
            throws IOException {

        return service.analyze(schema, objectName);
    }
}
