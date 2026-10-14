package spring.ai.example.spring_ai_demo.controller.v21;

/**
 * @author Tahereh Kasehpoor
 */



import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService.ConnectionStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v21/oracle")
@ConditionalOnProperty(
        prefix = "app.oracle",
        name = "enabled",
        havingValue = "true"
)
public class OracleMetadataController {

    private final OracleMetadataService service;

    public OracleMetadataController(OracleMetadataService service) {
        this.service = service;
    }

    @GetMapping("/connection")
    public ConnectionStatus connection() {
        return service.testConnection();
    }

//    http://localhost:9999/api/v21/oracle/objects?type=PACKAGE

//    http://localhost:9999/api/v21/oracle/objects?type=FUNCTION
    @GetMapping("/objects")
    public List<Map<String, Object>> objects(
            @RequestParam(required = false) String schema,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name) {
        return service.listObjects(schema, type, name);
    }

    @GetMapping("/arguments")
    public List<Map<String, Object>> arguments(
            @RequestParam(required = false) String schema,
            @RequestParam String name,
            @RequestParam(required = false) String packageName) {
        return service.listArguments(schema, name, packageName);
    }

    @GetMapping("/source")
    public List<Map<String, Object>> source(
            @RequestParam(required = false) String schema,
            @RequestParam String name,
            @RequestParam String type) {
        return service.getSource(schema, name, type);
    }

    @GetMapping("/dependencies")
    public List<Map<String, Object>> dependencies(
            @RequestParam(required = false) String schema,
            @RequestParam String name,
            @RequestParam String type) {
        return service.listDependencies(schema, name, type);
    }
}
