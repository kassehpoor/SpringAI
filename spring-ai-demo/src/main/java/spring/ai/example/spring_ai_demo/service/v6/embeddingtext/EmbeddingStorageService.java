package spring.ai.example.spring_ai_demo.service.v6.embeddingtext;

import spring.ai.example.spring_ai_demo.dto.v6.embeddingtext.SaveEmbeddingRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.embeddingtext.SaveEmbeddingResponseDTO;

/**
 * @author Tahereh Kasehpoor
 */

public interface EmbeddingStorageService {

    public SaveEmbeddingResponseDTO save(
            SaveEmbeddingRequestDTO request);
}
