package spring.ai.example.spring_ai_demo.config;

/**
 * @author Tahereh Kasehpoor
 */


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springAiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI Demo API @author Tahereh Kasehpoor ")
                        .description("API documentation for Spring AI Demo")
                        .version("v1.0.0"));
    }
}
