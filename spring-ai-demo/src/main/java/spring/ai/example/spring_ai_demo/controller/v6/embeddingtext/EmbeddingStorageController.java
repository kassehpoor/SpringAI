package spring.ai.example.spring_ai_demo.controller.v6.embeddingtext;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v6.embeddingtext.SaveEmbeddingRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.embeddingtext.SaveEmbeddingResponseDTO;
import spring.ai.example.spring_ai_demo.service.v6.embeddingtext.EmbeddingStorageService;

/**
 * @author Tahereh Kasehpoor
 */


@RestController
@RequestMapping("/api/v6/embeddings")
@RequiredArgsConstructor
public class EmbeddingStorageController {

    private final EmbeddingStorageService embeddingStorageService;

    @PostMapping
    public SaveEmbeddingResponseDTO save(
             @RequestBody SaveEmbeddingRequestDTO request) {

        return embeddingStorageService.save(request);
    }
}