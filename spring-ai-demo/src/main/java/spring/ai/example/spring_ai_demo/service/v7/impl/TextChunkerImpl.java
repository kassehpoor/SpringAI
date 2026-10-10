package spring.ai.example.spring_ai_demo.service.v7.impl;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.service.v7.TextChunker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tahereh Kasehpoor
 */


@Service
public class TextChunkerImpl implements TextChunker {

    @Override
    public List<String> chunk(
            String text,
            int chunkSize,
            int overlap) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text must not be blank"
            );
        }

        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "Chunk size must be greater than zero"
            );
        }

        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "Overlap must be >= 0 and < chunk size"
            );
        }

        List<String> chunks = new ArrayList<>();

        int start = 0;
        int step = chunkSize - overlap;

        while (start < text.length()) {

            int end = Math.min(
                    start + chunkSize,
                    text.length()
            );

            String chunk =
                    text.substring(start, end).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            start += step;
        }

        return chunks;
    }
}
