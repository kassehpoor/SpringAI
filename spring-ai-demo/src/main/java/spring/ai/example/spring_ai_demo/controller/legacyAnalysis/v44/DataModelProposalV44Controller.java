package spring.ai.example.spring_ai_demo.controller
        .legacyAnalysis.v44;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v44.DataModelProposalV44Request;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v44.DataModelProposalV44Service;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v44/data-model-proposals")
public class DataModelProposalV44Controller {

    private final DataModelProposalV44Service service;

    public DataModelProposalV44Controller(
            DataModelProposalV44Service service) {
        this.service = service;
    }

    /**
     تولید و ذخیره مدل داده پیشنهادی
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> generate(
            @RequestBody DataModelProposalV44Request request) {
        return service.generate(request);
    }

    /**
     دریافت یک مدل ذخیره‌شده
     */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return service.getProposal(id);
    }

    /**
     مشاهده مدل‌های ذخیره‌شده برای تحلیل شماره
     */
    @GetMapping
    public List<Map<String, Object>> list(
            @RequestParam Long analysisId) {
        return service.listByAnalysis(analysisId);
    }
}