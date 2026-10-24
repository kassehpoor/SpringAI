package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v42;

import java.time.OffsetDateTime;

public record OracleSnapshotSavedV42DTO(
        Long id,
        String schema,
        int tableCount,
        int relationshipCount,
        String status,
        OffsetDateTime createdAt
) {
}