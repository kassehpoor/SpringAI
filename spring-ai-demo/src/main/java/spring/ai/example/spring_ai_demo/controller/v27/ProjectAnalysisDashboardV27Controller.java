package spring.ai.example.spring_ai_demo.controller.v27;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v27.ProjectAnalysisDashboardV27Response;
import spring.ai.example.spring_ai_demo.service.v27.ProjectAnalysisDashboardV27Service;

@RestController
@RequestMapping("/api/v27/dashboard")
public class ProjectAnalysisDashboardV27Controller {

    private final ProjectAnalysisDashboardV27Service service;

    public ProjectAnalysisDashboardV27Controller(
            ProjectAnalysisDashboardV27Service service) {
        this.service = service;
    }

//    http://localhost:9999/api/v27/dashboard?schema=lsupcore0890

    @GetMapping
    public ProjectAnalysisDashboardV27Response getDashboard(
            @RequestParam(required = false) String schema) {
        return service.getDashboard(schema);
    }
}