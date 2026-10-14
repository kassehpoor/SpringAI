package spring.ai.example.spring_ai_demo.controller.v17;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.service.v17.DependencyChainService;
import spring.ai.example.spring_ai_demo.service.v17.DependencyChainService.ChainReport;

@RestController
@RequestMapping("/api/v17/dependencies")
public class DependencyChainController {

    private final DependencyChainService service;

    public DependencyChainController(DependencyChainService service) {
        this.service = service;
    }

    @GetMapping("/chain")
    public ChainReport analyze(
            @RequestParam String name,
            @RequestParam(defaultValue = "3") int depth,
            @RequestParam(defaultValue = "100") int limit) {

        return service.analyze(name, depth, limit);
    }
}

/**

 curl -X 'GET' \
 'http://localhost:9999/api/v17/dependencies/chain?name=RequestRESTService&depth=3&limit=50' \
 -H 'accept:

 */
