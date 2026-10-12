package spring.ai.example.spring_ai_demo.controller.v10;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import spring.ai.example.spring_ai_demo.dto.v10.CoordinatorRequest;
import spring.ai.example.spring_ai_demo.dto.v10.CoordinatorResponse;
import spring.ai.example.spring_ai_demo.service.v10.InsuranceCoordinatorService;
import spring.ai.example.spring_ai_demo.service.v10.tool.ToolResult;

@RestController
@RequestMapping("/api/v10/coordinator")
public class InsuranceCoordinatorController {

    private final InsuranceCoordinatorService coordinatorService;

    public InsuranceCoordinatorController(
            InsuranceCoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    /**
     {
     "question": "خدمات درمانی شامل چه مواردی است؟"
     }
     */
    /**
     {
     "question": "کدام کلاس‌های پروژه قدیمی به محاسبه مستمری وابسته هستند؟"
     }
     */

    /**
     {
     "question": "ساختار جدول‌های Oracle مربوط به مستمری چیست؟"
     }
     */
    @PostMapping("/ask")
    public ResponseEntity<CoordinatorResponse> ask(
            @RequestBody CoordinatorRequest request) {

        if (request == null
                || request.question() == null
                || request.question().isBlank()) {

            return ResponseEntity.badRequest().body(
                    new CoordinatorResponse(
                            request == null ? null : request.question(),
                            null,
                            false,
                            null,
                            "متن سؤال الزامی است."
                    )
            );
        }

        ToolResult result =
                coordinatorService.coordinate(request.question());

        CoordinatorResponse response =
                new CoordinatorResponse(
                        request.question(),
                        result.toolType(),
                        result.success(),
                        result.content(),
                        result.errorMessage()
                );

        if (!result.success()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }
}
