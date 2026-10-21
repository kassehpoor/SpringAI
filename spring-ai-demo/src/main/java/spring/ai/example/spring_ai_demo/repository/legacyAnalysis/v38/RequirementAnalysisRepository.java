
package spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v38;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;

@Repository
public class RequirementAnalysisRepository {

    private final JdbcTemplate jdbcTemplate;

    public RequirementAnalysisRepository(
            @Qualifier("postgresJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long saveDraft(
            String documentName,
            String sourceReference,
            String requirementText,
            String analysisJson,
            String modelName) {

        String sql = """
            INSERT INTO requirement_analysis
                (document_name, source_reference, requirement_text,
                 analysis_json, status, model_name)
            VALUES (?, ?, ?, CAST(? AS jsonb), 'DRAFT', ?)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                documentName,
                sourceReference,
                requirementText,
                analysisJson,
                modelName
        );
    }

    public String findAnalysisJsonById(Long id) {
        String sql = """
            SELECT analysis_json::text
            FROM requirement_analysis
            WHERE id = ?
            """;

        return jdbcTemplate.queryForObject(sql, String.class, id);
    }

    public int updateStatus(Long id, String status) {
        String sql = """
            UPDATE requirement_analysis
            SET status = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        return jdbcTemplate.update(sql, status, id);
    }
}
