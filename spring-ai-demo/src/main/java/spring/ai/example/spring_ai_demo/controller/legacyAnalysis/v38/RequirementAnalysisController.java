
package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v38;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v38.RequirementAnalysisRequest;
import spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v38.RequirementAnalysisRepository;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v38.RequirementAnalysisService;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v38/requirement-analysis")
public class RequirementAnalysisController {

    private final RequirementAnalysisService service;
    private final RequirementAnalysisRepository repository;

    public RequirementAnalysisController(
            RequirementAnalysisService service,
            RequirementAnalysisRepository repository) {

        this.service = service;
        this.repository = repository;
    }

    /**
     * تحلیل متن نیازمندی
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequirementAnalysisService.SavedRequirementAnalysis analyze(
            @Valid @RequestBody RequirementAnalysisRequest request) {

        return service.analyze(request);
    }

    /**
     * دریافت تحلیل ذخیره‌شده
     */
    @GetMapping("/{id}")
    public Map<String, Object> getAnalysis(@PathVariable Long id) {

        String analysisJson = repository.findAnalysisJsonById(id);

        if (analysisJson == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "تحلیل پیدا نشد: " + id
            );
        }

        return Map.of(
                "id", id,
                "analysis", analysisJson
        );
    }

    /**
     * تغییر وضعیت تحلیل
     */
    @PatchMapping("/{id}/status")
    public Map<String, Object> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");

        if (status == null
                || !Set.of("IN_REVIEW", "APPROVED", "REJECTED")
                .contains(status)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "وضعیت مجاز: IN_REVIEW, APPROVED, REJECTED"
            );
        }

        int updated = repository.updateStatus(id, status);

        if (updated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "تحلیل پیدا نشد: " + id
            );
        }

        return Map.of(
                "id", id,
                "status", status
        );
    }

    /**
     * تحلیل مستقیم فایل PDF

     فایل استخراج و تحلیل می‌شود و نتیجه با وضعیت DRAFT در PostgreSQL ذخیره خواهد شد.
     */
    @PostMapping(
            value = "/pdf",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<
            RequirementAnalysisService.SavedRequirementAnalysis
            > analyzePdf(
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "فایل PDF خالی است."
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null
                || !fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "فقط فایل PDF پذیرفته می‌شود."
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.analyzePdf(file));
    }
}
