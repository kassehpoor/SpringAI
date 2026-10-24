package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v42;

import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v42.OracleSnapshotSavedV42DTO;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v42.OracleSchemaSnapshotV42Service;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v42/oracle-snapshots")
public class OracleSchemaSnapshotV42Controller {

    private final OracleSchemaSnapshotV42Service service;

    public OracleSchemaSnapshotV42Controller(
            OracleSchemaSnapshotV42Service service) {
        this.service = service;
    }

    @PostMapping
    public OracleSnapshotSavedV42DTO create(
            @RequestParam String schema) {
        return service.createSnapshot(schema);
    }

    @GetMapping
    public List<Map<String, Object>> list(
            @RequestParam String schema) {
        return service.listSnapshots(schema);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable long id) {
        return service.getSnapshot(id);
    }
}