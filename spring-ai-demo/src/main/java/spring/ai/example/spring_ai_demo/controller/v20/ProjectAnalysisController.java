package spring.ai.example.spring_ai_demo.controller.v20;

/**
 * @author Tahereh Kasehpoor
 */


import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.MethodCallInfo;
import spring.ai.example.spring_ai_demo.service.v20.ProjectAnalysisFacadeService;
import spring.ai.example.spring_ai_demo.service.v20.ProjectAnalysisFacadeService.ClassDetail;
import spring.ai.example.spring_ai_demo.service.v20.ProjectAnalysisFacadeService.ClassSearchResult;
import spring.ai.example.spring_ai_demo.service.v20.ProjectAnalysisFacadeService.MethodSearchResult;
import spring.ai.example.spring_ai_demo.service.v20.ProjectAnalysisFacadeService.OracleStaticReport;
import spring.ai.example.spring_ai_demo.service.v20.ProjectAnalysisFacadeService.ProjectSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 با تست موفق این شش endpoint، لایهٔ یکپارچه‌سازی V20 آماده است. توجه کن که تحلیل Oracle در این مرحله هنوز Regex-based است و ممکن است در رشته‌ها و کامنت‌ها موارد اشتباه پیدا کند؛ همچنین خواندن پکیج‌ها و فانکشن‌های واقعی از دیتابیس انجام نمی‌شود.
 در V21 سراغ اتصال read-only به Oracle می‌رویم تا متادیتای واقعی پکیج‌ها، فانکشن‌ها، پروسیجرها و وابستگی‌های آن‌ها را استخراج کنیم.
 */
@RestController
@RequestMapping("/api/v20/project")
public class ProjectAnalysisController {

    private final ProjectAnalysisFacadeService service;

    public ProjectAnalysisController(ProjectAnalysisFacadeService service) {
        this.service = service;
    }

//    مار کلاس‌ها، متدها، فراخوانی‌ها و خطاهای Parse
    @GetMapping("/summary")
    public ProjectSummary summary() {
        return service.getSummary();
    }

//    جست‌وجوی کلاس‌ها
    @GetMapping("/classes")
    public List<ClassSearchResult> searchClasses(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "100") int limit) {
        return service.searchClasses(q, limit);
    }

//    جزئیات کلاس
    @GetMapping("/class-detail")
    public ClassDetail classDetail(
            @RequestParam("name") String name) {
        return service.getClassDetail(name);
    }

//    جست‌وجوی متدها
    @GetMapping("/methods")
    public List<MethodSearchResult> searchMethods(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String className,
            @RequestParam(defaultValue = "100") int limit) {
        return service.searchMethods(q, className, limit);
    }

//    جست‌وجوی فراخوانی‌ها
    @GetMapping("/calls")
    public List<MethodCallInfo> searchCalls(
            @RequestParam(defaultValue = "") String method,
            @RequestParam(defaultValue = "") String className,
            @RequestParam(defaultValue = "100") int limit) {
        return service.searchCalls(method, className, limit);
    }
//    تحلیل ایستای Oracle در سورس
    @GetMapping("/oracle-static-analysis")
    public OracleStaticReport analyzeOracleReferences(
            @RequestParam(defaultValue = "200") int limit)
            throws IOException {
        return service.analyzeOracleReferences(limit);
    }
}

