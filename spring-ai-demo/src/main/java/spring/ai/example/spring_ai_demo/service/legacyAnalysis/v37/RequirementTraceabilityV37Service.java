package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v37;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchResponseDTO;
import spring.ai.example.spring_ai_demo.dto.v36.OracleImpactReportV36Response;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v37.RequirementTraceabilityV37Request;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v37.RequirementTraceabilityV37Response;
import spring.ai.example.spring_ai_demo.service.v6.document.InsuranceDocumentSearchService;
import spring.ai.example.spring_ai_demo.service.v36.OracleImpactReportV36Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequirementTraceabilityV37Service {

    private final InsuranceDocumentSearchService documentSearchService;
    private final OracleImpactReportV36Service oracleImpactService;

    public RequirementTraceabilityV37Response analyze(
            RequirementTraceabilityV37Request request) throws IOException {

        List<String> warnings = new ArrayList<>();

        // 1. جست‌وجوی معنایی در مستندات موجود
        InsuranceDocumentSearchRequestDTO searchRequest =
                new InsuranceDocumentSearchRequestDTO();

        searchRequest.setQuery(request.getRequirement());
        searchRequest.setTopK(
                request.getTopK() == null ? 5 : request.getTopK());

        InsuranceDocumentSearchResponseDTO documentResponse =
                documentSearchService.search(searchRequest);

        if (documentResponse.getResults() == null
                || documentResponse.getResults().isEmpty()) {
            warnings.add("هیچ نتیجه مرتبطی در مستندات پیدا نشد.");
        }

        // 2. بررسی وابستگی Oracle و شواهد Java، در صورت ارائه اطلاعات
        OracleImpactReportV36Response oracleImpact = null;

        boolean hasOracleInfo =
                hasText(request.getSchema())
                        && hasText(request.getOracleObjectName())
                        && hasText(request.getOracleObjectType());

        if (hasOracleInfo) {
            int maxDepth = request.getMaxDepth() == null
                    ? 3
                    : request.getMaxDepth();

            oracleImpact = oracleImpactService.analyze(
                    request.getSchema(),
                    request.getOracleObjectName(),
                    request.getOracleObjectType(),
                    maxDepth
            );

            warnings.add(
                    "ارتباط‌های Java بر اساس تطبیق متنی هستند؛ "
                            + "این تطبیق به‌تنهایی وابستگی زمان اجرا را اثبات نمی‌کند."
            );
        } else {
            warnings.add(
                    "تحلیل Oracle اجرا نشد؛ برای اجرای آن schema، "
                            + "oracleObjectName و oracleObjectType را مشخص کنید."
            );
        }

        // 3. ساخت گزارش نهایی
        return RequirementTraceabilityV37Response.builder()
                .requirement(request.getRequirement())
                .documentMatches(documentResponse.getResults())
                .oracleImpact(oracleImpact)
                .warnings(warnings)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}