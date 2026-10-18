
package spring.ai.example.spring_ai_demo.service.v33;

import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.dto.v25.JavaOracleLinkV25Response;
import spring.ai.example.spring_ai_demo.dto.v32.OracleDependencyCascadeV32Response;
import spring.ai.example.spring_ai_demo.dto.v33.OracleJavaCascadeV33Response;
import spring.ai.example.spring_ai_demo.service.v25.JavaOracleLinkV25Service;
import spring.ai.example.spring_ai_demo.service.v32.OracleDependencyCascadeV32Service;

import java.io.IOException;
import java.util.*;

@Service
public class OracleJavaCascadeV33Service {

    private final OracleDependencyCascadeV32Service cascadeService;
    private final JavaOracleLinkV25Service javaLinkService;

    public OracleJavaCascadeV33Service(
            OracleDependencyCascadeV32Service cascadeService,
            JavaOracleLinkV25Service javaLinkService) {
        this.cascadeService = cascadeService;
        this.javaLinkService = javaLinkService;
    }

    public OracleJavaCascadeV33Response analyze(
            String schema,
            String objectName,
            String objectType,
            int maxDepth) throws IOException {

        var cascade = cascadeService.analyze(
                schema, objectName, objectType, maxDepth);

        List<OracleJavaCascadeV33Response.ObjectJavaMatches> evidence =
                new ArrayList<>();

        Set<String> affectedClasses = new HashSet<>();
        int totalMatches = 0;
        String projectName = null;

        for (OracleDependencyCascadeV32Response.DependencyNode node
                : cascade.objects()) {

            try {
                JavaOracleLinkV25Response response =
                        javaLinkService.analyze(
                                node.owner(), node.name());

                projectName = response.projectName();

                List<OracleJavaCascadeV33Response.JavaMatch> matches =
                        response.matches().stream()
                                .map(match ->
                                        new OracleJavaCascadeV33Response.JavaMatch(
                                                match.className(),
                                                match.methodName(),
                                                match.path(),
                                                match.line(),
                                                match.evidence()))
                                .toList();

                matches.stream()
                        .map(OracleJavaCascadeV33Response.JavaMatch::className)
                        .forEach(affectedClasses::add);

                totalMatches += matches.size();

                evidence.add(
                        new OracleJavaCascadeV33Response.ObjectJavaMatches(
                                node.owner(),
                                node.name(),
                                node.type(),
                                matches.size(),
                                matches,
                                null));

            } catch (Exception ex) {
                evidence.add(
                        new OracleJavaCascadeV33Response.ObjectJavaMatches(
                                node.owner(),
                                node.name(),
                                node.type(),
                                0,
                                List.of(),
                                ex.getMessage()));
            }
        }

        return new OracleJavaCascadeV33Response(
                projectName,
                cascade.schema(),
                cascade.rootObjectName(),
                cascade.rootObjectType(),
                cascade.objectCount(),
                totalMatches,
                affectedClasses.size(),
                cascade,
                List.copyOf(evidence),
                "STATIC_SOURCE_ANALYSIS_WITH_ORACLE_DEPENDENCIES",
                "Oracle dependencies come from ALL_DEPENDENCIES. Java evidence is based on textual source matches and does not prove runtime usage."
        );
    }
}
