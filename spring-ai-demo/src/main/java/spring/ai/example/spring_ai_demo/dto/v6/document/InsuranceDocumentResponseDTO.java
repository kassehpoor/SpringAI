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
//@Schema(description = "")
public class InsuranceDocumentResponseDTO {

    private Long id;
    private int embeddingDimensions;
    private String message;
}
