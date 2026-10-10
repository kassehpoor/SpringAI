package spring.ai.example.spring_ai_demo.service.v7.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v7.PdfChunkResponseDTO;
import spring.ai.example.spring_ai_demo.service.v4.PdfDocumentService;
import spring.ai.example.spring_ai_demo.service.v7.PdfChunkService;
import spring.ai.example.spring_ai_demo.service.v7.TextChunker;

import java.util.List;

/**
 * @author Tahereh Kasehpoor
 */
@Service
@RequiredArgsConstructor
public class PdfChunkServiceImpl
        implements PdfChunkService {

    private final PdfDocumentService pdfDocumentService;

    private final TextChunker textChunker;

    @Override
    public PdfChunkResponseDTO chunk(
            MultipartFile file) {

        Document document =
                pdfDocumentService.createDocument(file);

        String text =
                document.getText();

        List<String> chunks =
                textChunker.chunk(
                        text,
                        500,
                        100
                );

        return PdfChunkResponseDTO.builder()
                .fileName(
                        (String) document
                                .getMetadata()
                                .get("fileName")
                )
                .pageCount(
                        (Integer) document
                                .getMetadata()
                                .get("pageCount")
                )
                .totalChunks(chunks.size())
                .chunks(chunks)
                .build();
    }
}
