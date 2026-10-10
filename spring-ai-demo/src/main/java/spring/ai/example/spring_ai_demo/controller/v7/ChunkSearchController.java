package spring.ai.example.spring_ai_demo.controller.v7;

/**
 * @author Tahereh Kasehpoor
 */


import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchResultDTO;
import spring.ai.example.spring_ai_demo.service.v7.ChunkSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/v7/chunks")
@RequiredArgsConstructor
public class ChunkSearchController {

    private final ChunkSearchService chunkSearchService;

    /**
     {
     "query": "شرایط بازنشستگی بیمه شده چیست؟",
     "topK": 5
     }
     */
    @PostMapping("/search")
    public List<ChunkSearchResultDTO> search(
            @RequestBody ChunkSearchRequestDTO request) {

        return chunkSearchService.search(request);
    }
}

