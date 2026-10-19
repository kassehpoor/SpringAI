package spring.ai.example.spring_ai_demo.controller.v6.document;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchResponseDTO;
import spring.ai.example.spring_ai_demo.service.v6.document.InsuranceDocumentSearchService;

/**
 * @author Tahereh Kasehpoor
 */

@RestController
@RequestMapping("/api/v6/documents")
@RequiredArgsConstructor
public class InsuranceDocumentSearchController {

    private final InsuranceDocumentSearchService searchService;

    /**
     *
     {
     "query": "شرایط بازنشستگی بیمه شده چیست؟",
     "topK": 5
     }


    /**
     *
     insurance_document
     │
     ├── id
     ├── content
     ├── embedding       VECTOR(768)
     ├── created_at
     ├── file_name
     └── page_count
     */
    @PostMapping("/search")
    public InsuranceDocumentSearchResponseDTO search(
            @Valid @RequestBody InsuranceDocumentSearchRequestDTO request) {

        return searchService.search(request);
    }
}

/**
 نتیجه‌ی جستجوی تو هم منطقی است:

 id=1 → متن بازنشستگی → فاصله 0.0903 → بهترین نتیجه
 id=2 → همان PDF بازنشستگی با متن کامل‌تر → 0.1476
 id=6 → سابقه بیمه → 0.1840
 id=4 → مستمری بازماندگان → 0.2057
 id=3 → درمان → 0.2330

 یعنی pgvector واقعاً دارد بر اساس معنای متن‌ها ranking انجام می‌دهد، نه صرفاً جستجوی کلمه.

 یک نکته مهم هم از خروجی مشخص است: PDFBox متن فارسی PDFها را با ترتیب حروف/کلمات نامناسب استخراج کرده است، مثلاً:

 شده برای شود. بیمهشده بر اساس...

 این فعلاً برای یادگیری V6 مانع نیست، ولی در V7 یعنی RAG کیفیت Retrieval را تحت تأثیر قرار می‌دهد و آنجا به این
 */