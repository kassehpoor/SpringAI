package spring.ai.example.spring_ai_demo.service.v21;

/**
 * @author Tahereh Kasehpoor
 */

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(
        prefix = "app.oracle",
        name = "enabled",
        havingValue = "true"
)
public class OracleMetadataService {

    private final JdbcTemplate jdbc;
    private final String defaultSchema;
    private final int queryTimeout;
    private final int maxRows;

    public OracleMetadataService(
            @Qualifier("oracleMetadataDataSource") DataSource dataSource,
            @Value("${app.oracle.default-schema:}") String defaultSchema,
            @Value("${app.oracle.query-timeout-seconds:15}") int queryTimeout,
            @Value("${app.oracle.max-rows:5000}") int maxRows) {

        this.jdbc = new JdbcTemplate(dataSource);
        this.jdbc.setQueryTimeout(Math.max(1, queryTimeout));
        this.defaultSchema = defaultSchema == null
                ? "" : defaultSchema.trim().toUpperCase();
        this.queryTimeout = Math.max(1, queryTimeout);
        this.maxRows = Math.max(1, Math.min(maxRows, 5000));
    }

    public ConnectionStatus testConnection() {
        Map<String, Object> result = jdbc.queryForMap("""
                SELECT
                    SYS_CONTEXT('USERENV', 'SESSION_USER') AS SESSION_USER,
                    SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') AS CURRENT_SCHEMA
                FROM DUAL
                """);

        return new ConnectionStatus(
                true,
                stringValue(result, "SESSION_USER"),
                stringValue(result, "CURRENT_SCHEMA"),
                "Read-only metadata connection test succeeded."
        );
    }

    public List<Map<String, Object>> listObjects(
            String schema, String objectType, String name) {

        String effectiveSchema = normalizeSchema(schema);
        String type = normalizeOptional(objectType);
        String objectName = normalizeOptional(name);

        return jdbc.queryForList("""
                SELECT OWNER, OBJECT_NAME, OBJECT_TYPE, STATUS,
                       CREATED, LAST_DDL_TIME
                FROM ALL_OBJECTS
                WHERE OWNER = ?
                  AND (? IS NULL OR OBJECT_TYPE = ?)
                  AND (? IS NULL OR OBJECT_NAME LIKE ? ESCAPE '\\')
                ORDER BY OBJECT_TYPE, OBJECT_NAME
                FETCH FIRST %d ROWS ONLY
                """.formatted(maxRows),
                effectiveSchema,
                type, type,
                objectName, toLikePattern(objectName));
    }

    public List<Map<String, Object>> listArguments(
            String schema, String objectName, String packageName) {

        String effectiveSchema = normalizeSchema(schema);
        String object = requiredName(objectName, "objectName");
        String pkg = normalizeOptional(packageName);

        return jdbc.queryForList("""
                SELECT OWNER, PACKAGE_NAME, OBJECT_NAME, OVERLOAD,
                       ARGUMENT_NAME, POSITION, SEQUENCE, DATA_LEVEL,
                       IN_OUT, DATA_TYPE, TYPE_OWNER, TYPE_NAME,
                       DEFAULTED
                FROM ALL_ARGUMENTS
                WHERE OWNER = ?
                  AND OBJECT_NAME = ?
                  AND (PACKAGE_NAME = ? OR
                       (PACKAGE_NAME IS NULL AND ? IS NULL))
                ORDER BY SEQUENCE
                FETCH FIRST %d ROWS ONLY
                """.formatted(maxRows),
                effectiveSchema, object, pkg, pkg);
    }

    public List<Map<String, Object>> getSource(
            String schema, String name, String type) {

        String effectiveSchema = normalizeSchema(schema);
        String object = requiredName(name, "name");
        String objectType = requiredName(type, "type");

        if (!List.of("PACKAGE", "PACKAGE BODY",
                        "FUNCTION", "PROCEDURE", "TYPE", "TYPE BODY")
                .contains(objectType)) {
            throw new IllegalArgumentException(
                    "Unsupported source type: " + objectType);
        }

        return jdbc.queryForList("""
                SELECT OWNER, NAME, TYPE, LINE, TEXT
                FROM ALL_SOURCE
                WHERE OWNER = ? AND NAME = ? AND TYPE = ?
                ORDER BY LINE
                FETCH FIRST %d ROWS ONLY
                """.formatted(maxRows),
                effectiveSchema, object, objectType);
    }

    public List<Map<String, Object>> listDependencies(
            String schema, String name, String type) {

        String effectiveSchema = normalizeSchema(schema);
        String object = requiredName(name, "name");
        String objectType = requiredName(type, "type");

        return jdbc.queryForList("""
                SELECT OWNER, NAME, TYPE,
                       REFERENCED_OWNER, REFERENCED_NAME,
                       REFERENCED_TYPE, DEPENDENCY_TYPE
                FROM ALL_DEPENDENCIES
                WHERE OWNER = ? AND NAME = ? AND TYPE = ?
                ORDER BY REFERENCED_OWNER, REFERENCED_NAME
                FETCH FIRST %d ROWS ONLY
                """.formatted(maxRows),
                effectiveSchema, object, objectType);
    }

    private String normalizeSchema(String schema) {
        String value = schema == null || schema.isBlank()
                ? defaultSchema : schema.trim();

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "Oracle schema is required. Configure app.oracle.default-schema.");
        }

        return value.toUpperCase();
    }

    private String requiredName(String value, String field) {
        String result = normalizeOptional(value);

        if (result == null) {
            throw new IllegalArgumentException(field + " is required.");
        }

        return result;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private String toLikePattern(String name) {
        if (name == null) {
            return null;
        }

        String escaped = name
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        return "%" + escaped + "%";
    }

    private String stringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : value.toString();
    }

    public record ConnectionStatus(
            boolean connected,
            String sessionUser,
            String currentSchema,
            String message) {
    }
}
