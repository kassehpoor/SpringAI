
package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v38;

import java.util.List;

/**
 چرا این ساختار؟

 - requirements: نیازمندی‌های سامانه
 - businessRules: قواعد کسب‌وکار استخراج‌شده
 - entities: موجودیت‌ها و ویژگی‌های پیشنهادی
 - relationships: ارتباط بین موجودیت‌ها
 - constraints: محدودیت‌های صریح سند
 - ambiguities: مواردی که تحلیل‌گر باید تعیین تکلیف کند
 - sourceReference: ارجاع به صفحه یا بخش منبع، برای جلوگیری از گم‌شدن منشأ اطلاعات
 نکته: این DTO قرارداد خروجی پیشنهادی V38 است؛ هنوز خروجی واقعی مدل هوش مصنوعی یا داده‌ی تأییدشده نیست.
 */
public record RequirementAnalysisDTO(
        String documentName,
        String status,
        List<RequirementItem> requirements,
        List<BusinessRuleItem> businessRules,
        List<EntityItem> entities,
        List<RelationshipItem> relationships,
        List<String> constraints,
        List<String> ambiguities
) {

    public record RequirementItem(
            String id,
            String title,
            String description,
            String sourceReference
    ) {}

    public record BusinessRuleItem(
            String id,
            String description,
            String sourceReference
    ) {}

    public record EntityItem(
            String name,
            List<String> attributes,
            String sourceReference
    ) {}

    public record RelationshipItem(
            String sourceEntity,
            String targetEntity,
            String relationshipType,
            String description
    ) {}
}
