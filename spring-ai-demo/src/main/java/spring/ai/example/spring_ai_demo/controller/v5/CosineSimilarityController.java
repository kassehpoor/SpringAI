package spring.ai.example.spring_ai_demo.controller.v5;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.service.v5.CosineSimilarityService;



/**
 * @author Tahereh Kasehpoor
 */

@RestController
@RequestMapping("/api/v5/similarity")
@RequiredArgsConstructor
public class CosineSimilarityController {

    private final CosineSimilarityService similarityService;


    /**
     *
     Text
     ↓
     EmbeddingModel
     ↓
     float[]
     ↓
     Cosine Similarity
     */


    @PostMapping
    public double calculate(
            @RequestBody SimilarityRequest request) {

        return similarityService.calculate(
                request.vectorA(),
                request.vectorB()
        );
    }

    public record SimilarityRequest(
            float[] vectorA,
            float[] vectorB
    ) {
    }


//TODO:
    /**
     *
     textA
     ↓
     EmbeddingModel
     ↓
     vectorA
     \
     → CosineSimilarity → similarity
     /
     textB
     ↓
     EmbeddingModel
     ↓
     vectorB
     */


}