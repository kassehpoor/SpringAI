package spring.ai.example.spring_ai_demo.controller.v24;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v24.MethodCallChainResponse;
import spring.ai.example.spring_ai_demo.service.v24.MethodCallChainV24Service;

@RestController
@RequestMapping("/api/v24/call-chain")
public class MethodCallChainV24Controller {

    private final MethodCallChainV24Service service;

    public MethodCallChainV24Controller(MethodCallChainV24Service service) {
        this.service = service;
    }

    @GetMapping
    public MethodCallChainResponse findChain(
            @RequestParam String sourceClass,
            @RequestParam String sourceMethod,
            @RequestParam(defaultValue = "5") int maxDepth) {

        return service.findChain(sourceClass, sourceMethod, maxDepth);
    }
}