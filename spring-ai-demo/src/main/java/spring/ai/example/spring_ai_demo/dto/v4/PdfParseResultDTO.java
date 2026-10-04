package spring.ai.example.spring_ai_demo.dto.v4;

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
public class PdfParseResultDTO {

    private int pageCount;

    private String text;
}
