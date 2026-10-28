package spring.ai.example.spring_ai_demo.controller.legacyAnalysis.v41;

import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v41.OracleErdV41DTO;
import spring.ai.example.spring_ai_demo.service.legacyAnalysis.v41.OracleErdV41Service;

@RestController
@RequestMapping("/api/v41/oracle-erd")
public class OracleErdV41Controller {

    private final OracleErdV41Service service;

    public OracleErdV41Controller(OracleErdV41Service service) {
        this.service = service;
    }

    /**
     این API به OracleErdV41Service وصل است. سرویس از OracleMetadataService نسخه V21 اطلاعات جدول‌ها، ستون‌ها، کلیدهای اصلی و کلیدهای خارجی را می‌گیرد و خروجی Mermaid ER Diagram می‌سازد.

     در پاسخ باید فیلدهایی مشابه این‌ها ببینی:
     - schema: نام Schema
     - mermaid: متن نمودار با ساختار erDiagram
     - tableCount: تعداد جدول‌های شناسایی‌شده
     - relationshipCount: تعداد روابط استخراج‌شده
     - status: وضعیت خروجی
     این با مقدار "erd" که در پیشنهاد V44 فرق دارد؛ "erd" یک نمودار معتبر نیست، اما V41 برای ساخت متن واقعی Mermaid پیاده‌سازی دارد.
     */

    @GetMapping
    public OracleErdV41DTO generate(@RequestParam String schema) {
        return service.generate(schema);
    }
}