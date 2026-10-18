
package spring.ai.example.spring_ai_demo.service.v36;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v36.OracleImpactReportV36Response;
import spring.ai.example.spring_ai_demo.service.v25.JavaOracleLinkV25Service;
import spring.ai.example.spring_ai_demo.service.v32.OracleDependencyCascadeV32Service;

import java.io.IOException;
import java.util.*;

@Service
public class OracleImpactReportV36Service {

    private final OracleDependencyCascadeV32Service cascadeService;
    private final JavaOracleLinkV25Service javaLinkService;

    public OracleImpactReportV36Service(
            OracleDependencyCascadeV32Service cascadeService,
            JavaOracleLinkV25Service javaLinkService) {
        this.cascadeService = cascadeService;
        this.javaLinkService = javaLinkService;
    }

    public OracleImpactReportV36Response analyze(
            String schema,
            String objectName,
            String objectType,
            int maxDepth) throws IOException {

        var cascade = cascadeService.analyze(
                schema, objectName, objectType, maxDepth);

        List<OracleImpactReportV36Response.ImpactItem> items =
                new ArrayList<>();

        Set<String> affectedClasses = new HashSet<>();
        int totalMatches = 0;
        String projectName = null;

        for (var node : cascade.objects()) {
            List<OracleImpactReportV36Response.JavaEvidence> evidence =
                    new ArrayList<>();

            try {
                var response = javaLinkService.analyze(
                        node.owner(), node.name());

                projectName = response.projectName();

                evidence = response.matches().stream()
                        .map(match ->
                                new OracleImpactReportV36Response.JavaEvidence(
                                        match.className(),
                                        match.methodName(),
                                        match.path(),
                                        match.line(),
                                        match.evidence()))
                        .toList();

            } catch (IOException ex) {
                throw ex;
            }

            evidence.stream()
                    .map(OracleImpactReportV36Response.JavaEvidence::className)
                    .forEach(affectedClasses::add);

            totalMatches += evidence.size();

            int classCount = (int) evidence.stream()
                    .map(OracleImpactReportV36Response.JavaEvidence::className)
                    .distinct()
                    .count();

            String priority = classCount >= 10
                    ? "HIGH_REVIEW_PRIORITY"
                    : classCount >= 3
                    ? "MEDIUM_REVIEW_PRIORITY"
                    : classCount > 0
                    ? "LOW_REVIEW_PRIORITY"
                    : "NO_JAVA_TEXT_MATCH";

            items.add(
                    new OracleImpactReportV36Response.ImpactItem(
                            node.depth(),
                            node.owner(),
                            node.name(),
                            node.type(),
                            evidence.size(),
                            classCount,
                            priority,
                            evidence));
        }

        items.sort(
                Comparator.comparingInt(
                                OracleImpactReportV36Response.ImpactItem::depth)
                        .thenComparing(
                                Comparator.comparingInt(
                                                OracleImpactReportV36Response.ImpactItem
                                                        ::affectedClassCount)
                                        .reversed()));

        return new OracleImpactReportV36Response(
                projectName,
                cascade.schema(),
                cascade.rootObjectName(),
                cascade.rootObjectType(),
                cascade.objectCount(),
                totalMatches,
                affectedClasses.size(),
                List.copyOf(items),
                "Oracle links are based on ALL_DEPENDENCIES. Java links are textual source matches. Priority indicates review effort, not confirmed runtime impact."
        );
    }
}
