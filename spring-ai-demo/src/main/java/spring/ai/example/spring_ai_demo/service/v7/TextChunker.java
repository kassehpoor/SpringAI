package spring.ai.example.spring_ai_demo.service.v7;

import java.util.List;

/**
 * @author Tahereh Kasehpoor
 */
public interface TextChunker {

    List<String> chunk(
            String text,
            int chunkSize,
            int overlap
    );
}
