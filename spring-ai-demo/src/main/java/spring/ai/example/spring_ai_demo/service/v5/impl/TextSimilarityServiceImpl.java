package spring.ai.example.spring_ai_demo.service.v5.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v5.TextSimilarityRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v5.TextSimilarityResponseDTO;
import spring.ai.example.spring_ai_demo.service.v5.CosineSimilarityService;
import spring.ai.example.spring_ai_demo.service.v5.TextSimilarityService;

/**
 * @author Tahereh Kasehpoor
 */


/**
 *
 * "شرایط بازنشستگی..."
 *         ↓
 * EmbeddingModel
 *         ↓
 * [0.12, -0.31, 0.82, ...]
 *
 *
 * "برای بازنشسته شدن..."
 *         ↓
 * EmbeddingModel
 *         ↓
 * [0.10, -0.29, 0.79, ...]
 *
 *
 *         ↓
 * Cosine Similarity
 *         ↓
 *
 *        0.91
 */

@Service
@RequiredArgsConstructor
public class TextSimilarityServiceImpl
        implements TextSimilarityService {

    private final EmbeddingModel embeddingModel;

    private final CosineSimilarityService cosineSimilarityService;

    @Override
    public TextSimilarityResponseDTO calculate(
            TextSimilarityRequestDTO request) {

        // 1. Create embedding for text A
        float[] vectorA =
                embeddingModel.embed(request.getTextA());

        // 2. Create embedding for text B
        float[] vectorB =
                embeddingModel.embed(request.getTextB());

        // 3. Calculate cosine similarity
        double similarity =
                cosineSimilarityService.calculate(
                        vectorA,
                        vectorB
                );

        // 4. Build response
        return TextSimilarityResponseDTO.builder()
                .textA(request.getTextA())
                .textB(request.getTextB())
                .similarity(similarity)
                .build();
    }
}
