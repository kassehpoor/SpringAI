
package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v38;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v38.RequirementAnalysisRequest;
import spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v38.RequirementAnalysisRepository;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v38.RequirementAnalysisService;


import java.util.Map;

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
     توجه: این API فعلاً وضعیت را تغییر می‌دهد، اما هنوز احراز هویت و نقش تأییدکننده ندارد. تا پیش از افزودن کنترل دسترسی، آن را در محیط آزمایشی اجرا کن و به محیط عملیاتی متصل نکن.
     */

    /**

     {
     "documentName": "تحلیل سامانه مستمری",
     "sourceReference": "بخش شرایط بازنشستگی",
     "requirementText": "شرایط بازنشستگی بیمه‌شده بر اساس سن، سابقه پرداخت حق بیمه و ضوابط قانونی تعیین می‌شود. سامانه باید امکان بررسی شرایط بازنشستگی را فراهم کند."
     }

     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequirementAnalysisService.SavedRequirementAnalysis analyze(
            @Valid @RequestBody RequirementAnalysisRequest request) {
        return service.analyze(request);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getAnalysis(@PathVariable Long id) {
        return Map.of(
                "id", id,
                "analysis", repository.findAnalysisJsonById(id)
        );
    }

    @PatchMapping("/{id}/status")
    public Map<String, Object> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");

        if (!java.util.Set.of("IN_REVIEW", "APPROVED", "REJECTED")
                .contains(status)) {
            throw new IllegalArgumentException(
                    "وضعیت مجاز: IN_REVIEW, APPROVED, REJECTED"
            );
        }

        int updated = repository.updateStatus(id, status);

        if (updated == 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "تحلیل پیدا نشد: " + id
            );
        }

        return Map.of("id", id, "status", status);
    }
}
