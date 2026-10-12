package spring.ai.example.spring_ai_demo.controller.v14;

/**
 * @author Tahereh Kasehpoor
 */

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import spring.ai.example.spring_ai_demo.service.v14.OracleStaticAnalysisService;

import java.util.Map;

@RestController
@RequestMapping("/api/v14/oracle")
public class OracleAnalysisController {

    private final OracleStaticAnalysisService analysisService;

    public OracleAnalysisController(
            OracleStaticAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/analyze")
    public ResponseEntity<?> analyze() {
        try {
            return ResponseEntity.ok(analysisService.analyze());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
