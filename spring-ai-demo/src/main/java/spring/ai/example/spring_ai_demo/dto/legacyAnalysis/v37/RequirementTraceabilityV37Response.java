package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v37;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchResultDTO;
import spring.ai.example.spring_ai_demo.dto.v36.OracleImpactReportV36Response;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementTraceabilityV37Response {

    private String requirement;

    /**
     * نتایج مرتبط از مستندات ذخیره‌شده در pgvector
     */
    private List<InsuranceDocumentSearchResultDTO> documentMatches;

    /**
     * گزارش وابستگی Oracle و شواهد متنی Java
     */
    private OracleImpactReportV36Response oracleImpact;

    /**
     * توضیحات و محدودیت‌های تحلیل
     */
    private List<String> warnings;
}