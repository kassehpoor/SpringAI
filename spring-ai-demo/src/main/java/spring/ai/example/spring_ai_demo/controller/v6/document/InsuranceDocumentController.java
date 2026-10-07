package spring.ai.example.spring_ai_demo.controller.v6.document;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentResponseDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.PdfEmbeddingResponseDTO;
import spring.ai.example.spring_ai_demo.service.v6.document.InsuranceDocumentService;

/**
 * @author Tahereh Kasehpoor
 */


@RestController
@RequestMapping("/api/v6/documents")
@RequiredArgsConstructor
public class InsuranceDocumentController {

    private final InsuranceDocumentService documentService;


    /**
     {
     "content": "شرایط بازنشستگی بیمه شده بر اساس سن و سابقه پرداخت حق بیمه تعیین می‌شود."
     }
     */


    /**
     {
     "content": "بیمه شده برای دریافت مستمری بازنشستگی باید شرایط قانونی مربوط به سن و سابقه را داشته باشد."
     }
     */

    //V6.2 — جست‌وجوی اسناد مشابه با pgvector از طریق Spring Boot خواهد بود

    @PostMapping
    public InsuranceDocumentResponseDTO save(
             @RequestBody InsuranceDocumentRequestDTO request) {

        return documentService.save(request);
    }



    @PostMapping(
            value = "/pdf",
            consumes = "multipart/form-data"
    )
    public PdfEmbeddingResponseDTO savePdf(
            @RequestPart("file") MultipartFile file) {

        return documentService.savePdf(file);
    }
}