package spring.ai.example.spring_ai_demo.controller.v13;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.MethodCallInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v13/search")
public class ProjectSearchController {

    private final ProjectStructureIndexService indexService;

    public ProjectSearchController(
            ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    /**
     http://localhost:9999/api/v13/search/classes?q=Pension&limit=50
     */
    /**
     * جست‌وجوی کلاس‌ها بر اساس نام یا بخشی از نام
     */
    @GetMapping("/classes")
    public ResponseEntity<?> searchClasses(
            @RequestParam String q,
            @RequestParam(defaultValue = "50") int limit) {

        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            return indexNotReady();
        }

        if (q.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "پارامتر q نباید خالی باشد."));
        }

        int safeLimit = Math.max(1, Math.min(limit, 200));
        String keyword = q.trim().toLowerCase();

        List<Map<String, Object>> results =
                index.classes().values().stream()
                        .filter(c ->
                                c.name().toLowerCase().contains(keyword)
                                        || c.qualifiedName()
                                        .toLowerCase().contains(keyword))
                        .limit(safeLimit)
                        .map(c -> Map.<String, Object>of(
                                "name", c.name(),
                                "qualifiedName", c.qualifiedName(),
                                "kind", c.kind(),
                                "path", c.path(),
                                "methodCount", c.methods().size(),
                                "methods", c.methods().stream()
                                        .limit(20)
                                        .toList()
                        ))
                        .toList();

        return ResponseEntity.ok(Map.of(
                "projectName", index.projectName(),
                "query", q,
                "resultCount", results.size(),
                "limit", safeLimit,
                "results", results
        ));
    }

    /**
     http://localhost:9999/api/v13/search/calls?method=calculate&limit=100
     */
    /**
     * جست‌وجوی محل فراخوانی یک متد
     */
    @GetMapping("/calls")
    public ResponseEntity<?> searchMethodCalls(
            @RequestParam String method,
            @RequestParam(defaultValue = "100") int limit) {

        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            return indexNotReady();
        }

        if (method.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "پارامتر method نباید خالی باشد."));
        }

        int safeLimit = Math.max(1, Math.min(limit, 500));
        String keyword = method.trim().toLowerCase();

        List<Map<String, Object>> results =
                index.calls().stream()
                        .filter(call -> call.methodName()
                                .toLowerCase()
                                .contains(keyword))
                        .limit(safeLimit)
                        .map(call -> Map.<String, Object>of(
                                "methodName", call.methodName(),
                                "sourceClass", call.sourceClass(),
                                "sourceMethod", call.sourceMethod(),
                                "scope", call.scope(),
                                "path", call.path(),
                                "line", call.line()
                        ))
                        .toList();

        return ResponseEntity.ok(Map.of(
                "projectName", index.projectName(),
                "query", method,
                "resultCount", results.size(),
                "limit", safeLimit,
                "results", results
        ));
    }

    /**
     http://localhost:9999/api/v13/search/class-details?name=RequestRESTService
     */
    /**
     * مشاهده جزئیات یک کلاس
     */
    @GetMapping("/class-details")
    public ResponseEntity<?> getClassDetails(
            @RequestParam String name) {

        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            return indexNotReady();
        }

        if (name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "پارامتر name نباید خالی باشد."));
        }

        String query = name.trim();

        // ابتدا نام کامل کلاس را بررسی می‌کنیم.
        ClassInfo classInfo = index.classes().get(query);

        // اگر نام کامل پیدا نشد، نام ساده کلاس را بررسی می‌کنیم.
        if (classInfo == null) {
            List<ClassInfo> matches =
                    index.classes().values().stream()
                            .filter(c ->
                                    c.name().equalsIgnoreCase(query)
                                            || c.qualifiedName()
                                            .equalsIgnoreCase(query))
                            .toList();

            if (matches.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "کلاس پیدا نشد.",
                                "name", query
                        ));
            }

            if (matches.size() > 1) {
                return ResponseEntity.ok(Map.of(
                        "message", "چند کلاس با این نام پیدا شد؛ نام کامل را ارسال کن.",
                        "matches", matches.stream()
                                .map(ClassInfo::qualifiedName)
                                .toList()
                ));
            }

            classInfo = matches.get(0);
        }

        ClassInfo found = classInfo;

        List<Map<String, Object>> calls =
                index.calls().stream()
                        .filter(call -> call.sourceClass()
                                .equals(found.qualifiedName()))
                        .limit(200)
                        .map(call -> Map.<String, Object>of(
                                "methodName", call.methodName(),
                                "sourceMethod", call.sourceMethod(),
                                "scope", call.scope(),
                                "line", call.line()
                        ))
                        .toList();

        return ResponseEntity.ok(Map.of(
                "projectName", index.projectName(),
                "name", found.name(),
                "qualifiedName", found.qualifiedName(),
                "kind", found.kind(),
                "path", found.path(),
                "packageName", found.packageName(),
                "imports", found.imports(),
                "inheritance", found.inheritance(),
                "methods", found.methods(),
                "methodCalls", calls
        ));
    }

    private ResponseEntity<?> indexNotReady() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "نمایه پروژه آماده نیست.",
                        "message", "ابتدا ZIP را از طریق API مرحله V12 بارگذاری کن."
                ));
    }
}

