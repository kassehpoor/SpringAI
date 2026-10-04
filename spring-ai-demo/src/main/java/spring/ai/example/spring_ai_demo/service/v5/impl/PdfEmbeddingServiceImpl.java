package spring.ai.example.spring_ai_demo.service.v5.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.service.v4.PdfDocumentService;
import spring.ai.example.spring_ai_demo.service.v5.PdfEmbeddingService;


/**
 * @author Tahereh Kasehpoor
 */

@Service
@RequiredArgsConstructor
public class PdfEmbeddingServiceImpl implements PdfEmbeddingService {

    private final PdfDocumentService pdfDocumentService;
    private final EmbeddingModel embeddingModel;

    @Override
    public float[] createEmbedding(MultipartFile file) {

        // 1. PDF -> Spring AI Document
        Document document = pdfDocumentService.createDocument(file);

        // 2. Document -> Embedding Vector
        return embeddingModel.embed(document);
    }


//    public EmbeddingResponseDTO createEmbedding(String text) {
//
//        if (text == null || text.isBlank()) {
//            throw new IllegalArgumentException(
//                    "Text must not be empty"
//            );
//        }
//
//        float[] vector =
//                embeddingModel.embed(text);
//
//        return EmbeddingResponseDTO.builder()
//                .text(text)
//                .dimension(vector.length)
////                .vector(vector)
//                .build();
//    }

}
