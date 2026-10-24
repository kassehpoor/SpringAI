package spring.ai.example.spring_ai_demo.controller
        .legacyAnalysis.v43;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.service
        .legacyAnalysis.v38.RequirementAnalysisService;

@RestController
@RequestMapping("/api/v43/business-analysis")
public class BusinessTextAnalysisV43Controller {

    private final RequirementAnalysisService analysisService;

    public BusinessTextAnalysisV43Controller(
            RequirementAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping(
            value = "/text-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public RequirementAnalysisService.SavedRequirementAnalysis
    analyzeTextFile(@RequestParam("file") MultipartFile file) {

        return analysisService.analyzeTextFile(file);
    }
}