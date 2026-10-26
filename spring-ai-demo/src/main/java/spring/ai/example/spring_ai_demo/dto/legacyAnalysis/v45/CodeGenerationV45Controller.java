package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v45;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v45.CodeGenerationV45Service;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v45/code-generation")
public class CodeGenerationV45Controller {

    private final CodeGenerationV45Service service;

    public CodeGenerationV45Controller(CodeGenerationV45Service service) {
        this.service = service;
    }

    //http://localhost:9999/api/v45/code-generation?proposalId=1&packageName=ir.tamin.pension

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> generate(
            @RequestParam Long proposalId,
            @RequestParam String packageName) {

        return service.generate(proposalId, packageName);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return service.getGeneratedCode(id);
    }

    @GetMapping
    public List<Map<String, Object>> list(
            @RequestParam Long proposalId) {
        return service.listByProposal(proposalId);
    }
}