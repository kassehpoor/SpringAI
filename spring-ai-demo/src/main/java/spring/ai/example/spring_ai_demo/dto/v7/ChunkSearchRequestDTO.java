package spring.ai.example.spring_ai_demo.dto.v7;

/**
 * @author Tahereh Kasehpoor
 */


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunkSearchRequestDTO {

    private String query;

    private Integer topK = 5;
}
