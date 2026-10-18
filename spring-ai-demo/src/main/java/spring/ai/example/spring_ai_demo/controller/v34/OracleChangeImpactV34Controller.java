
package spring.ai.example.spring_ai_demo.controller.v34;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.dto.v34.OracleChangeImpactV34Response;
import spring.ai.example.spring_ai_demo.service.v34.OracleChangeImpactV34Service;

import java.io.IOException;

@RestController
@RequestMapping("/api/v34/oracle-change-impact")
public class OracleChangeImpactV34Controller {

    private final OracleChangeImpactV34Service service;

    public OracleChangeImpactV34Controller(
            OracleChangeImpactV34Service service) {
        this.service = service;
    }

    /**
     http://localhost:9999/api/v34/oracle-change-impact?schema=CLSU_CANPEN&objectName=LSU_REQ&objectType=TABLE&utm_source=chatgpt.com





     */

    @GetMapping
    public OracleChangeImpactV34Response analyze(
            @RequestParam(required = false) String schema,
            @RequestParam(required = false) String objectName,
            @RequestParam(required = false) String objectType)
            throws IOException {

        return service.analyze(schema, objectName, objectType);
    }
}
