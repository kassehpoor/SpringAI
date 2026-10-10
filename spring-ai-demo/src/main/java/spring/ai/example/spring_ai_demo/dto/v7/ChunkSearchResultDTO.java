package spring.ai.example.spring_ai_demo.dto.v7;

/**
 * @author Tahereh Kasehpoor
 */


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkSearchResultDTO {

    private Long documentId;

    private String fileName;

    private Integer chunkIndex;

    private String content;

    private Integer pageNumber;

    /**
     distance: فاصله کسینوسی؛ هرچه کمتر باشد، بردارها به هم نزدیک‌ترند.
     */
    private Double distance;

    /**
     cosineSimilarity:هرچه بیشتر باشد، شباهت بیشتر است.
     */
    private Double cosineSimilarity;

}
