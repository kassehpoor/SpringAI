
package spring.ai.example.spring_ai_demo.service.v35;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v34.OracleChangeImpactV34Response;
import spring.ai.example.spring_ai_demo.dto.v35.OracleImpactPriorityV35Response;
import spring.ai.example.spring_ai_demo.service.v34.OracleChangeImpactV34Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
public class OracleImpactPriorityV35Service {

    private final OracleChangeImpactV34Service v34Service;

    public OracleImpactPriorityV35Service(
            OracleChangeImpactV34Service v34Service) {
        this.v34Service = v34Service;
    }

    public OracleImpactPriorityV35Response analyze(
            String schema,
            String objectName,
            String objectType) throws IOException {

        OracleChangeImpactV34Response report =
                v34Service.analyze(schema, objectName, objectType);

        List<OracleImpactPriorityV35Response.PrioritizedObject> priorities =
                report.objects().stream()
                        .sorted(
                                Comparator.comparingInt(
                                                OracleChangeImpactV34Response.ObjectChange
                                                        ::affectedClassCount)
                                        .reversed()
                                        .thenComparing(
                                                Comparator.comparingInt(
                                                                OracleChangeImpactV34Response.ObjectChange
                                                                        ::javaMatchCount)
                                                        .reversed()))
                        .map(change ->
                                new OracleImpactPriorityV35Response.PrioritizedObject(
                                        0,
                                        change.objectName(),
                                        change.objectType(),
                                        change.status(),
                                        change.lastDdlTime(),
                                        change.javaMatchCount(),
                                        change.affectedClassCount(),
                                        priority(change)))
                        .toList();

        List<OracleImpactPriorityV35Response.PrioritizedObject> ranked =
                java.util.stream.IntStream.range(0, priorities.size())
                        .mapToObj(i -> {
                            var item = priorities.get(i);
                            return new OracleImpactPriorityV35Response.PrioritizedObject(
                                    i + 1,
                                    item.objectName(),
                                    item.objectType(),
                                    item.status(),
                                    item.lastDdlTime(),
                                    item.javaMatchCount(),
                                    item.affectedClassCount(),
                                    item.reviewPriority());
                        })
                        .toList();

        return new OracleImpactPriorityV35Response(
                report.projectName(),
                report.schema(),
                ranked.size(),
                ranked,
                "Ranking is based on textual Java matches only. It is a review aid, not a measured risk score."
        );
    }

    private String priority(
            OracleChangeImpactV34Response.ObjectChange change) {

        if (change.affectedClassCount() >= 10) {
            return "HIGH_REVIEW_PRIORITY";
        }

        if (change.affectedClassCount() >= 3) {
            return "MEDIUM_REVIEW_PRIORITY";
        }

        if (change.affectedClassCount() > 0) {
            return "LOW_REVIEW_PRIORITY";
        }

        return "NO_JAVA_TEXT_MATCH";
    }
}
