package spring.ai.example.spring_ai_demo.service.v4.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v4.PdfDocumentResponseDTO;
import spring.ai.example.spring_ai_demo.dto.v4.PdfParseResultDTO;
import spring.ai.example.spring_ai_demo.service.v4.PdfDocumentService;
import spring.ai.example.spring_ai_demo.service.v4.PdfParserService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tahereh Kasehpoor
 */


@Service
@RequiredArgsConstructor
public class PdfDocumentServiceImpl implements PdfDocumentService {

    private final PdfParserService pdfParserService;

//TODO:

    /**
     * ✓ file != null
     * ✓ empty check
     * ✓ size limit
     * ✓ extension check
     * ✓ MIME check
     * ✓ PDF signature / magic bytes
     */


    /**
     *
     V4 — PDF READER

     PDF
     │
     ▼
     MultipartFile
     │
     ▼
     PDFBox Parser
     │
     ▼
     Extract Text
     │
     ▼
     Spring AI Document
     ┌─────┴─────┐
     ▼           ▼
     Text       Metadata
     │
     ┌──────────┼──────────┐
     ▼          ▼          ▼
     fileName  contentType  pageCount
     */

    @Override
    public PdfDocumentResponseDTO parse(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "PDF file is empty"
            );
        }

        PdfParseResultDTO result =
                pdfParserService.parse(file);

        Document document = new Document(
                result.getText(),
                Map.of(
                        "fileName",
                        file.getOriginalFilename(),

                        "contentType",
                        file.getContentType(),

                        "pageCount",
                        result.getPageCount()
                )
        );

        return PdfDocumentResponseDTO.builder()
                .fileName(
                        (String) document.getMetadata()
                                .get("fileName")
                )
                .contentType(
                        (String) document.getMetadata()
                                .get("contentType")
                )
                .fileSize(file.getSize())
                .pageCount(
                        (Integer) document.getMetadata()
                                .get("pageCount")
                )
                .text(document.getText())
                .build();
    }

    @Override
    public Document createDocument(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "PDF file is empty"
            );
        }

        PdfParseResultDTO result =
                pdfParserService.parse(file);

        return new Document(
                result.getText(),
                Map.of(
                        "fileName",
                        file.getOriginalFilename(),

                        "contentType",
                        file.getContentType(),

                        "pageCount",
                        result.getPageCount()
                )
        );
    }
}
