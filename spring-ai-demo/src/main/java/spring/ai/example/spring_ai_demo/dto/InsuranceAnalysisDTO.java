package spring.ai.example.spring_ai_demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tahereh Kasehpoor
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceAnalysisDTO {

    private String title;

    private String summary;

    private String category;

    private List<String> requiredDocuments;

    private boolean requiresExpertReview;
}
