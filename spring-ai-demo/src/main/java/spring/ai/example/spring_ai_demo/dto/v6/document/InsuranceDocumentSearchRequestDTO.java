package spring.ai.example.spring_ai_demo.dto.v6.document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tahereh Kasehpoor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceDocumentSearchRequestDTO {

    @NotBlank(message = "Query must not be blank")
    private String query;

    @Min(value = 1, message = "Top K must be at least 1")
    private int topK;
}

/**+
 *
 * {
 *   "query": "شرایط بازنشستگی بیمه شده چیست؟",
 *   "topK": 3
 * }
 */