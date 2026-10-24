package spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v44;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DataModelProposalV44Repository {

    private final JdbcTemplate jdbc;

    public DataModelProposalV44Repository(
            @Qualifier("postgresJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long save(
            Long analysisId,
            String schema,
            String tableNamesJson,
            String proposalJson,
            String mermaid) {

        return jdbc.queryForObject("""
                INSERT INTO data_model_proposal
                    (analysis_id, schema_name, source_table_names,
                     proposal_json, mermaid_erd, status)
                VALUES (?, ?, CAST(? AS JSONB), CAST(? AS JSONB), ?, 'DRAFT')
                RETURNING id
                """,
                Long.class,
                analysisId,
                schema,
                tableNamesJson,
                proposalJson,
                mermaid
        );
    }

    public Map<String, Object> findById(Long id) {
        return jdbc.queryForMap("""
                SELECT id, analysis_id, schema_name, proposal_json,
                       mermaid_erd, status, created_at
                FROM data_model_proposal
                WHERE id = ?
                """, id);
    }

    public List<Map<String, Object>> findByAnalysisId(Long analysisId) {
        return jdbc.queryForList("""
                SELECT id, analysis_id, schema_name, status, created_at
                FROM data_model_proposal
                WHERE analysis_id = ?
                ORDER BY created_at DESC
                LIMIT 50
                """, analysisId);
    }
}