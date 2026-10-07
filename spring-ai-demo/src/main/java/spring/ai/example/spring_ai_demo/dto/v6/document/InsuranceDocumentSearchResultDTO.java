package spring.ai.example.spring_ai_demo.dto.v6.document;

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
public class InsuranceDocumentSearchResultDTO {

    private Long id;

    private String content;

    private double distance;
}
