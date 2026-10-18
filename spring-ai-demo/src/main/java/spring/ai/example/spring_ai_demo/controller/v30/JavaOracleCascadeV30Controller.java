
package spring.ai.example.spring_ai_demo.controller.v30;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.dto.v30.JavaOracleCascadeV30Response;
import spring.ai.example.spring_ai_demo.service.v30.JavaOracleCascadeV30Service;

import java.io.IOException;

@RestController
@RequestMapping("/api/v30/oracle-cascade")
public class JavaOracleCascadeV30Controller {

    private final JavaOracleCascadeV30Service service;

    public JavaOracleCascadeV30Controller(
            JavaOracleCascadeV30Service service) {
        this.service = service;
    }

    /**
     <a href="http://localhost:9999/api/v30/oracle-cascade?objectNames=lsu_req&schema=CLSU_CANPEN">...</a>
     */
    @GetMapping
    public JavaOracleCascadeV30Response analyze(
            @RequestParam String objectNames,
            @RequestParam(required = false) String schema)
            throws IOException {

        return service.analyze(schema, objectNames);
    }
}
