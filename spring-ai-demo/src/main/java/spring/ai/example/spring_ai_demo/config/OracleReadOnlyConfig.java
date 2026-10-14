package spring.ai.example.spring_ai_demo.config;

/**
 * @author Tahereh Kasehpoor
 */

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Conditional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(
        prefix = "app.oracle",
        name = "enabled",
        havingValue = "true"
)
public class OracleReadOnlyConfig {

    @Bean(name = "oracleMetadataDataSource", destroyMethod = "close")
    public DataSource oracleMetadataDataSource(
            @Value("${app.oracle.url}") String url,
            @Value("${app.oracle.username}") String username,
            @Value("${app.oracle.password}") String password) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Oracle read-only credentials are not configured.");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("oracle-metadata-readonly-pool");
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(5000);
        config.setValidationTimeout(3000);
        config.setReadOnly(true);

        return new HikariDataSource(config);
    }
}
