package spring.ai.example.spring_ai_demo.controller.v11;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v11.ProjectAnalysisResponse;
import spring.ai.example.spring_ai_demo.service.v11.ProjectZipAnalysisService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v11/project")
public class ProjectZipAnalysisController {

    private final ProjectZipAnalysisService analysisService;

    public ProjectZipAnalysisController(
            ProjectZipAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping(
            value = "/analyze-zip",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> analyzeZip(
            @RequestPart("file") MultipartFile file) {

        try {
            ProjectAnalysisResponse response =
                    analysisService.analyze(file);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", exception.getMessage()
                    )
            );

        } catch (IOException exception) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of(
                            "success", false,
                            "message", "خطا در خواندن فایل ZIP."
                    )
            );
        }
    }
}