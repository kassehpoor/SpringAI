package spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v42;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class OracleSchemaSnapshotV42Repository {

    private final JdbcTemplate jdbc;

    public OracleSchemaSnapshotV42Repository(
            @Qualifier("postgresJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long save(
            String schema,
            int tableCount,
            int relationshipCount,
            String mermaid,
            String snapshotJson) {

        String sql = """
                INSERT INTO oracle_schema_snapshot
                    (schema_name, table_count, relationship_count,
                     mermaid_erd, snapshot_json, status)
                VALUES (?, ?, ?, ?, CAST(? AS JSONB), 'DRAFT')
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, schema);
            ps.setInt(2, tableCount);
            ps.setInt(3, relationshipCount);
            ps.setString(4, mermaid);
            ps.setString(5, snapshotJson);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Could not retrieve generated snapshot ID");
        }

        return key.longValue();
    }

    public List<Map<String, Object>> findBySchema(String schema) {
        return jdbc.queryForList("""
                SELECT id, schema_name, table_count, relationship_count,
                       status, created_at
                FROM oracle_schema_snapshot
                WHERE schema_name = ?
                ORDER BY created_at DESC
                LIMIT 100
                """, schema);
    }

    public Map<String, Object> findById(long id) {
        return jdbc.queryForMap("""
                SELECT id, schema_name, table_count, relationship_count,
                       mermaid_erd, snapshot_json, status, created_at
                FROM oracle_schema_snapshot
                WHERE id = ?
                """, id);
    }
}