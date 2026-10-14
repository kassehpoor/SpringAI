package spring.ai.example.spring_ai_demo.controller.v19;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.service.v19.MethodCallChainService;
import spring.ai.example.spring_ai_demo.service.v19.MethodCallChainService.MethodChainReport;

@RestController
@RequestMapping("/api/v19/dependencies")
public class MethodCallChainController {

    private final MethodCallChainService service;

    public MethodCallChainController(MethodCallChainService service) {
        this.service = service;
    }

    @GetMapping("/method-calls")
    public MethodChainReport analyze(
            @RequestParam("name") String className,
            @RequestParam(defaultValue = "100") int limit) {

        return service.analyze(className, limit);
    }
}

