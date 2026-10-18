
package spring.ai.example.spring_ai_demo.controller.v31;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.dto.v31.OracleDependenciesV31Response;
import spring.ai.example.spring_ai_demo.service.v31.OracleDependenciesV31Service;

@RestController
@RequestMapping("/api/v31/oracle-dependencies")
public class OracleDependenciesV31Controller {

    private final OracleDependenciesV31Service service;

    public OracleDependenciesV31Controller(
            OracleDependenciesV31Service service) {
        this.service = service;
    }

    @GetMapping
    public OracleDependenciesV31Response analyze(
            @RequestParam String objectName,
            @RequestParam String objectType,
            @RequestParam(required = false) String schema) {

        return service.analyze(schema, objectName, objectType);
    }
}
