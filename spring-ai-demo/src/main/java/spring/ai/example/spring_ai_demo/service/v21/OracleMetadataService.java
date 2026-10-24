package spring.ai.example.spring_ai_demo.service.v21;


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

    public List<Map<String, Object>> listColumns(
            String schema,
            String tableName) {

        String effectiveSchema = normalizeSchema(schema);
        String table = requiredName(tableName, "tableName");

        return jdbc.queryForList("""
            SELECT
                OWNER,
                TABLE_NAME,
                COLUMN_NAME,
                DATA_TYPE,
                DATA_LENGTH,
                DATA_PRECISION,
                DATA_SCALE,
                NULLABLE,
                COLUMN_ID
            FROM ALL_TAB_COLUMNS
            WHERE OWNER = ?
              AND TABLE_NAME = ?
            ORDER BY COLUMN_ID
            FETCH FIRST %d ROWS ONLY
            """.formatted(maxRows),
                effectiveSchema,
                table);
    }

    public List<Map<String, Object>> listConstraints(
            String schema,
            String tableName) {

        String effectiveSchema = normalizeSchema(schema);
        String table = requiredName(tableName, "tableName");

        return jdbc.queryForList("""
            SELECT
                c.OWNER,
                c.TABLE_NAME,
                c.CONSTRAINT_NAME,
                c.CONSTRAINT_TYPE,
                cc.COLUMN_NAME,
                cc.POSITION,
                c.STATUS,
                c.R_OWNER AS REFERENCED_OWNER,
                rc.TABLE_NAME AS REFERENCED_TABLE,
                rcc.COLUMN_NAME AS REFERENCED_COLUMN
            FROM ALL_CONSTRAINTS c
            LEFT JOIN ALL_CONS_COLUMNS cc
                ON cc.OWNER = c.OWNER
               AND cc.CONSTRAINT_NAME = c.CONSTRAINT_NAME
               AND cc.TABLE_NAME = c.TABLE_NAME
            LEFT JOIN ALL_CONSTRAINTS rc
                ON rc.OWNER = c.R_OWNER
               AND rc.CONSTRAINT_NAME = c.R_CONSTRAINT_NAME
            LEFT JOIN ALL_CONS_COLUMNS rcc
                ON rcc.OWNER = rc.OWNER
               AND rcc.CONSTRAINT_NAME = rc.CONSTRAINT_NAME
               AND rcc.TABLE_NAME = rc.TABLE_NAME
               AND rcc.POSITION = cc.POSITION
            WHERE c.OWNER = ?
              AND c.TABLE_NAME = ?
              AND c.CONSTRAINT_TYPE IN ('P', 'R', 'U', 'C')
            ORDER BY c.CONSTRAINT_NAME, cc.POSITION
            FETCH FIRST %d ROWS ONLY
            """.formatted(maxRows),
                effectiveSchema,
                table);
    }

    public List<Map<String, Object>> listForeignKeys(String schema) {

        String effectiveSchema = normalizeSchema(schema);

        return jdbc.queryForList("""
            SELECT
                fk.OWNER AS OWNER,
                fk.TABLE_NAME AS TABLE_NAME,
                fk.CONSTRAINT_NAME AS CONSTRAINT_NAME,
                fkc.COLUMN_NAME AS COLUMN_NAME,
                fkc.POSITION AS COLUMN_POSITION,
                pk.OWNER AS REFERENCED_OWNER,
                pk.TABLE_NAME AS REFERENCED_TABLE,
                pkc.COLUMN_NAME AS REFERENCED_COLUMN,
                fk.STATUS AS STATUS
            FROM ALL_CONSTRAINTS fk
            JOIN ALL_CONS_COLUMNS fkc
              ON fkc.OWNER = fk.OWNER
             AND fkc.CONSTRAINT_NAME = fk.CONSTRAINT_NAME
             AND fkc.TABLE_NAME = fk.TABLE_NAME
            JOIN ALL_CONSTRAINTS pk
              ON pk.OWNER = fk.R_OWNER
             AND pk.CONSTRAINT_NAME = fk.R_CONSTRAINT_NAME
            JOIN ALL_CONS_COLUMNS pkc
              ON pkc.OWNER = pk.OWNER
             AND pkc.CONSTRAINT_NAME = pk.CONSTRAINT_NAME
             AND pkc.TABLE_NAME = pk.TABLE_NAME
             AND pkc.POSITION = fkc.POSITION
            WHERE fk.OWNER = ?
              AND fk.CONSTRAINT_TYPE = 'R'
            ORDER BY fk.TABLE_NAME, fk.CONSTRAINT_NAME,
                     fkc.POSITION
            FETCH FIRST %d ROWS ONLY
            """.formatted(maxRows), effectiveSchema);
    }

    public record ConnectionStatus(
            boolean connected,
            String sessionUser,
            String currentSchema,
            String message) {
    }
}
