package spring.ai.example.spring_ai_demo.controller.v8;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.v8.ToolCallingResponseDTO;
import spring.ai.example.spring_ai_demo.service.v8.ToolCallingService;


@RestController
@RequestMapping("/api/v8/tools")
public class ToolCallingController {

    private final ToolCallingService toolCallingService;

    public ToolCallingController(ToolCallingService toolCallingService) {
        this.toolCallingService = toolCallingService;
    }

    @PostMapping("/ask")
    public ToolCallingResponseDTO ask(@RequestBody Request request) {

        String answer = toolCallingService.ask(request.question());

        return new ToolCallingResponseDTO(request.question(), answer);
    }

    public record Request(String question) {
    }
}
