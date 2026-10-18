
package spring.ai.example.spring_ai_demo.dto.v33;

import spring.ai.example.spring_ai_demo.dto.v32.OracleDependencyCascadeV32Response;

import java.util.List;

public record OracleJavaCascadeV33Response(
        String projectName,
        String schema,
        String rootObjectName,
        String rootObjectType,
        int oracleObjectCount,
        int javaMatchCount,
        int affectedClassCount,
        OracleDependencyCascadeV32Response oracleCascade,
        List<ObjectJavaMatches> javaEvidence,
        String analysisMode,
        String warning
) {
    public record ObjectJavaMatches(
            String owner,
            String objectName,
            String objectType,
            int matchCount,
            List<JavaMatch> matches,
            String error
    ) {}

    public record JavaMatch(
            String className,
            String methodName,
            String path,
            int line,
            String evidence
    ) {}
}
