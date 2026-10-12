package spring.ai.example.spring_ai_demo.controller.v9;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.service.v9.InsuranceAgentService;

/**
 * @author Tahereh Kasehpoor
 */



@RestController
@RequestMapping("/api/v9/agent")
public class InsuranceAgentController {

    private final InsuranceAgentService agentService;

    public InsuranceAgentController(
            InsuranceAgentService agentService) {
        this.agentService = agentService;
    }

    /**
     Question → ChunkSearchService (V7) → Retrieved Context → ChatClient → Answer
     */
    @PostMapping("/ask")
    public AgentResponse ask(@RequestBody AgentRequest request) {
        return new AgentResponse(
                request.question(),
                agentService.ask(request.question()));
    }

    public record AgentRequest(String question) {}

    public record AgentResponse(
            String question,
            String answer) {}
}

