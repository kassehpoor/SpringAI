package spring.ai.example.spring_ai_demo.dto.v7;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tahereh Kasehpoor
 */

/**
 *
 * {
 *   "question": "شرایط بازنشستگی بیمه شده چیست؟",
 *   "topK": 3
 * }
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceRagRequestDTO {

    @NotBlank(message = "Question must not be blank")
    private String question;

    @Min(value = 1, message = "Top K must be at least 1")
    private int topK;
}