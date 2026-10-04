package spring.ai.example.spring_ai_demo.service.v5.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v5.ThreeTextSimilarityRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v5.ThreeTextSimilarityResponseDTO;
import spring.ai.example.spring_ai_demo.service.v5.CosineSimilarityService;
import spring.ai.example.spring_ai_demo.service.v5.ThreeTextSimilarityService;


/**
 * @author Tahereh Kasehpoor
 */

@Service
@RequiredArgsConstructor
public class ThreeTextSimilarityServiceImpl
        implements ThreeTextSimilarityService {

    private final EmbeddingModel embeddingModel;

    private final CosineSimilarityService cosineSimilarityService;

    @Override
    public ThreeTextSimilarityResponseDTO calculate(
            ThreeTextSimilarityRequestDTO request) {

        // 1. Create embeddings
        float[] vectorA =
                embeddingModel.embed(request.getTextA());

        float[] vectorB =
                embeddingModel.embed(request.getTextB());

        float[] vectorC =
                embeddingModel.embed(request.getTextC());

        // 2. Calculate similarities
        double similarityAB =
                cosineSimilarityService.calculate(
                        vectorA,
                        vectorB
                );

        double similarityAC =
                cosineSimilarityService.calculate(
                        vectorA,
                        vectorC
                );

        double similarityBC =
                cosineSimilarityService.calculate(
                        vectorB,
                        vectorC
                );

        // 3. Build response
        return ThreeTextSimilarityResponseDTO.builder()
                .textA(request.getTextA())
                .textB(request.getTextB())
                .textC(request.getTextC())
                .similarityAB(similarityAB)
                .similarityAC(similarityAC)
                .similarityBC(similarityBC)
                .build();
    }
}
