package spring.ai.example.spring_ai_demo.service.v4.impl;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v4.PdfParseResultDTO;
import spring.ai.example.spring_ai_demo.service.v4.PdfParserService;

import java.io.IOException;

/**
 * @author Tahereh Kasehpoor
 */


@Service
@RequiredArgsConstructor
public class PdfParserServiceImpl implements PdfParserService {


    /**
     *
     * PDF
     *  ↓
     * Text extraction
     *  ↓
     * String
     *
     */


    @Override
    public PdfParseResultDTO parse(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is empty");
        }

        try (PDDocument document =
                     Loader.loadPDF(file.getBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();

            String text = stripper.getText(document);

            return PdfParseResultDTO.builder()
                    .pageCount(document.getNumberOfPages())
                    .text(text)
                    .build();

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not parse PDF",
                    ex
            );
        }
    }


/**
 *
 * org.springframework.ai.document.Document
 *  ├── text
 *  └── metadata
 *
 *
 *  مثال
 *Document
 *  ├── text:
 *  │      "ماده ۱..."
 *  │
 *  └── metadata:
 *         fileName = law1.pdf
 *         pageCount = 10
 *
 *
 *این مفهوم در ادامه برای Embedding و RAG بسیار مهم می‌شود.
 *
 * inv v4:
 *
 * PDF
 *  ↓
 * Text
 *  ↓
 * Document
 *
 *
 *
 * in v5:
 *Document
 *  ↓
 * Embedding
 *  ↓
 * Vector
 *
 *
 *
 */


    // TODO:
/**
 *
 * PDF
 *  ↓
 * Image
 *  ↓
 * OCR
 *
 */
}
