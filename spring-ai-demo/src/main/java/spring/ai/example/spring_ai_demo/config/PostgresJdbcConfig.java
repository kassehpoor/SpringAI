
package spring.ai.example.spring_ai_demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class PostgresJdbcConfig {

    @Bean(name = "postgresDataSource", destroyMethod = "close")
    @Primary
    public DataSource postgresDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}")
            String driverClassName) {

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        config.setPoolName("postgres-pgvector-pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(5000);

        return new HikariDataSource(config);
    }

    @Bean(name = "postgresJdbcTemplate")
    @Primary
    public JdbcTemplate postgresJdbcTemplate(
            @Qualifier("postgresDataSource")
            DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }
}


/**
 چرا این نسخه؟
 - postgresDataSource اتصال PostgreSQL را مستقل از Oracle ایجاد می‌کند.
 - @Primary روی DataSource باعث می‌شود Spring برای تزریق عمومی DataSource و اتصال پیش‌فرض Hibernate، PostgreSQL را انتخاب کند.
 - @Primary روی postgresJdbcTemplate باعث می‌شود شش سرویس V6 و V7 که فیلد معمولی JdbcTemplate دارند، از PostgreSQL استفاده کنند.
 - OracleMetadataService همچنان با @Qualifier("oracleMetadataDataSource") اتصال اختصاصی Oracle خودش را می‌گیرد.
 */