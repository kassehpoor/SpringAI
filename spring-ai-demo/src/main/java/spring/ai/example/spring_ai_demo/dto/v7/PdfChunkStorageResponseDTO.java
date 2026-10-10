package spring.ai.example.spring_ai_demo.dto.v7;

/**
 * @author Tahereh Kasehpoor
 */



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdfChunkStorageResponseDTO {

    private Long documentId;

    private String fileName;

    private Integer pageCount;

    private Integer totalChunks;

    private Integer embeddingDimensions;

    private String message;
}

