package spring.ai.example.spring_ai_demo.service.v7;

import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchResultDTO;

import java.util.List;

/**
 * @author Tahereh Kasehpoor
 */
public interface ChunkSearchService {

    List<ChunkSearchResultDTO> search(
            ChunkSearchRequestDTO request
    );
}
