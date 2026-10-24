package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v41;

import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v41.OracleErdV41DTO;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v41.OracleErdV41Service;

@RestController
@RequestMapping("/api/v41/oracle-erd")
public class OracleErdV41Controller {

    private final OracleErdV41Service service;

    public OracleErdV41Controller(OracleErdV41Service service) {
        this.service = service;
    }

    @GetMapping
    public OracleErdV41DTO generate(@RequestParam String schema) {
        return service.generate(schema);
    }
}