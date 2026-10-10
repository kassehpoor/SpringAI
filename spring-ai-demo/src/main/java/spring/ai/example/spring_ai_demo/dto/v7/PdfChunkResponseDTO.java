package spring.ai.example.spring_ai_demo.dto.v7;

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
public class PdfChunkResponseDTO {

    private String fileName;

    private Integer pageCount;

    private Integer totalChunks;

    private List<String> chunks;
}