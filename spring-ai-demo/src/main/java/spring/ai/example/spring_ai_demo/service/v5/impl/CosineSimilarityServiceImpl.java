package spring.ai.example.spring_ai_demo.service.v5.impl;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.service.v5.CosineSimilarityService;



/**
 * @author Tahereh Kasehpoor
 */

@Service
public class CosineSimilarityServiceImpl
        implements CosineSimilarityService {

    @Override
    public double calculate(
            float[] vectorA,
            float[] vectorB) {

        if (vectorA == null || vectorB == null) {
            throw new IllegalArgumentException(
                    "Vectors must not be null"
            );
        }

        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimension"
            );
        }

        if (vectorA.length == 0) {
            throw new IllegalArgumentException(
                    "Vectors must not be empty"
            );
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {

            dotProduct +=
                    vectorA[i] * vectorB[i];

            normA +=
                    vectorA[i] * vectorA[i];

            normB +=
                    vectorB[i] * vectorB[i];
        }

        if (normA == 0 || normB == 0) {
            throw new IllegalArgumentException(
                    "Cannot calculate similarity for zero vector"
            );
        }

        return dotProduct /
                (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
