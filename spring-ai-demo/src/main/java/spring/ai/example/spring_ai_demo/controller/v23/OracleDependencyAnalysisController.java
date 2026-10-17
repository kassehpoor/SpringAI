
package spring.ai.example.spring_ai_demo.controller.v23;

/**
 * @author Tahereh Kasehpoor
 */

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.service.v23.OracleDependencyAnalysisService;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 نکته: این تحلیل بر اساس متن کد Java است. اگر SQL در چند خط ساخته شده باشد، نام شیء با متغیر ساخته شود یا از طریق متد دیگری فراخوانی شود، ممکن است ارتباط واقعی شناسایی نشود. همچنین unresolved صرفاً خطوط مشکوک را پیدا می‌کند و نتیجه آن لزوماً خطا یا وابستگی نامعتبر نیست.
 */


@RestController
@RequestMapping("/api/v23/dependencies")
@ConditionalOnProperty(
        prefix = "app.oracle",
        name = "enabled",
        havingValue = "true"
)
public class OracleDependencyAnalysisController {

    private final OracleDependencyAnalysisService service;

    public OracleDependencyAnalysisController(
            OracleDependencyAnalysisService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(
            @RequestParam(required = false) String schema)
            throws IOException {

        return service.summary(schema);
    }

    @GetMapping("/java-to-oracle")
    public List<Map<String, Object>> javaToOracle(
            @RequestParam(required = false) String schema,
            @RequestParam String name) throws IOException {

        return service.javaToOracle(schema, name);
    }

    @GetMapping("/oracle-to-java")
    public List<Map<String, Object>> oracleToJava(
            @RequestParam(required = false) String schema,
            @RequestParam String name) throws IOException {

        return service.oracleToJava(schema, name);
    }

    @GetMapping("/unresolved")
    public List<Map<String, Object>> unresolved(
            @RequestParam(required = false) String schema,
            @RequestParam(defaultValue = "100") int limit)
            throws IOException {

        return service.unresolved(schema, limit);
    }
}
