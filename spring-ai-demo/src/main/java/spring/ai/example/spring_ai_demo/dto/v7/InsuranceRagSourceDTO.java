package spring.ai.example.spring_ai_demo.dto.v7;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsuranceRagSourceDTO {

    private Long documentId;

    private String fileName;

    private Integer chunkIndex;

    private String content;

    private Integer pageNumber;

    private double distance;

    private double cosineSimilarity;
}

