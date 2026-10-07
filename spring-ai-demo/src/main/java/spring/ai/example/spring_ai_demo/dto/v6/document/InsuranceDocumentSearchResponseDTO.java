package spring.ai.example.spring_ai_demo.dto.v6.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tahereh Kasehpoor
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceDocumentSearchResponseDTO {

    private String query;

    private int topK;

    private List<InsuranceDocumentSearchResultDTO> results;
}
