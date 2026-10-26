package spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v45;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class GeneratedCodeV45Repository {

    private final JdbcTemplate jdbc;

    public GeneratedCodeV45Repository(
            @Qualifier("postgresJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long save(
            Long proposalId,
            String packageName,
            String filesJson) {

        return jdbc.queryForObject("""
                INSERT INTO generated_code_v45
                    (proposal_id, package_name, files_json)
                VALUES (?, ?, CAST(? AS JSONB))
                RETURNING id
                """,
                Long.class,
                proposalId,
                packageName,
                filesJson
        );
    }

    public Map<String, Object> findById(Long id) {
        return jdbc.queryForMap("""
                SELECT id, proposal_id, package_name, files_json,
                       status, created_at
                FROM generated_code_v45
                WHERE id = ?
                """, id);
    }

    public List<Map<String, Object>> findByProposalId(Long proposalId) {
        return jdbc.queryForList("""
                SELECT id, proposal_id, package_name, status, created_at
                FROM generated_code_v45
                WHERE proposal_id = ?
                ORDER BY created_at DESC
                LIMIT 50
                """, proposalId);
    }
}