
package spring.ai.example.spring_ai_demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.project-index")
public record ProjectIndexProperties(String storageDir) {
}
