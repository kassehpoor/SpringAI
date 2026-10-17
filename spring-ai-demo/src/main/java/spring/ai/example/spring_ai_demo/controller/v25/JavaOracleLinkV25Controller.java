package spring.ai.example.spring_ai_demo.controller.v25;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v25.JavaOracleLinkV25Response;
import spring.ai.example.spring_ai_demo.service.v25.JavaOracleLinkV25Service;

import java.io.IOException;



@RestController
@RequestMapping("/api/v25/oracle-links")
public class JavaOracleLinkV25Controller {

    private final JavaOracleLinkV25Service service;

    public JavaOracleLinkV25Controller(
            JavaOracleLinkV25Service service) {
        this.service = service;
    }

//    http://localhost:9999/api/v25/oracle-links?schema=lsupcore0890&objectName=LSU_CANPEN
    @GetMapping
    public JavaOracleLinkV25Response analyze(
            @RequestParam(required = false) String schema,
            @RequestParam String objectName) throws IOException {

        return service.analyze(schema, objectName);
    }
}