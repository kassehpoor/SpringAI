//package spring.ai.example.spring_ai_demo.controller.v4;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//import spring.ai.example.spring_ai_demo.dto.v4.PdfDocumentResponseDTO;
//import spring.ai.example.spring_ai_demo.dto.v4.PdfUploadResponseDTO;
//import spring.ai.example.spring_ai_demo.service.v4.PdfDocumentService;
//import spring.ai.example.spring_ai_demo.service.v4.PdfService;
//
///**
// * @author Tahereh Kasehpoor
// */
//
//
///**
// *
// Swagger
// │
// │ MultipartFile
// ▼
// Controller
// │
// ▼
// Service
// │
// ▼
// PDFBox
// │
// ▼
// Text
// │
// ▼
// Spring AI Document
// │
// ▼
// DTO
// │
// ▼
// JSON
// *
// */
//
//@RestController
//@RequestMapping("/api/v4/pdf")
//@RequiredArgsConstructor
//public class PdfController {
//
//    private final PdfService pdfService;
//    private final PdfDocumentService pdfDocumentService;
//
//
//
//    @PostMapping(
//            value = "/upload",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public PdfUploadResponseDTO upload(
//            @RequestParam("file") MultipartFile file) {
//
//        return pdfService.upload(file);
//    }
//
//    @PostMapping(
//            value = "/parse",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public PdfDocumentResponseDTO parse(
//            @RequestParam("file") MultipartFile file) {
//
//        return pdfDocumentService.parse(file);
//    }
//
//}