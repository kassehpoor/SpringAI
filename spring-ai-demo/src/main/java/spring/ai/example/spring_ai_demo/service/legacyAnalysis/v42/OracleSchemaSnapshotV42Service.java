package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v42;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v42.OracleSnapshotSavedV42DTO;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v41.OracleErdV41Service;
import spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v42.OracleSchemaSnapshotV42Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OracleSchemaSnapshotV42Service {

    private final OracleErdV41Service erdService;
    private final OracleSchemaSnapshotV42Repository repository;
    private final ObjectMapper objectMapper;

    public OracleSchemaSnapshotV42Service(
            OracleErdV41Service erdService,
            OracleSchemaSnapshotV42Repository repository,
            ObjectMapper objectMapper) {
        this.erdService = erdService;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OracleSnapshotSavedV42DTO createSnapshot(String schema) {
        if (schema == null || schema.isBlank()) {
            throw new IllegalArgumentException("Schema is required");
        }

        String normalizedSchema = schema.trim().toUpperCase(Locale.ROOT);

        var erd = erdService.generate(normalizedSchema);

        // فقط متادیتای مورد نیاز را ذخیره می‌کنیم؛ نه پاسخ کامل ERD در HTTP
        Map<String, Object> snapshot = Map.of(
                "schema", erd.schema(),
                "tableCount", erd.tableCount(),
                "relationshipCount", erd.relationshipCount(),
                "mermaid", erd.mermaid()
        );

        final String json;
        try {
            json = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Could not serialize schema snapshot", e);
        }

        Long id = repository.save(
                normalizedSchema,
                erd.tableCount(),
                erd.relationshipCount(),
                erd.mermaid(),
                json
        );

        return new OracleSnapshotSavedV42DTO(
                id,
                normalizedSchema,
                erd.tableCount(),
                erd.relationshipCount(),
                "DRAFT",
                OffsetDateTime.now()
        );
    }

    public List<Map<String, Object>> listSnapshots(String schema) {
        return repository.findBySchema(schema.trim().toUpperCase(Locale.ROOT));
    }

    public Map<String, Object> getSnapshot(long id) {
        return repository.findById(id);
    }
}