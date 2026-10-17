package spring.ai.example.spring_ai_demo.controller.v26;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v26.DependencyGraphV26Response;
import spring.ai.example.spring_ai_demo.service.v26.DependencyGraphV26Service;

import java.io.IOException;

@RestController
@RequestMapping("/api/v26/dependency-graph")
public class DependencyGraphV26Controller {

    private final DependencyGraphV26Service service;

    public DependencyGraphV26Controller(
            DependencyGraphV26Service service) {
        this.service = service;
    }

//    http://localhost:9999/api/v26/dependency-graph?schema=lsupcore0890&objectName=LSU_CANPEN&maxMatches=100

    /**
     نکته: این نسخه گراف را برای یک شیء Oracle می‌سازد و ارتباط را بر اساس تطبیق نام در سورس ثبت می‌کند؛ هنوز SQLهای داینامیک، نام‌های مستعار یا وابستگی قطعی حاصل از تحلیل SQL را تشخیص نمی‌دهد.
     */

    @GetMapping
    public DependencyGraphV26Response buildGraph(
            @RequestParam(required = false) String schema,
            @RequestParam String objectName,
            @RequestParam(defaultValue = "100") int maxMatches)
            throws IOException {

        return service.buildGraph(schema, objectName, maxMatches);
    }
}