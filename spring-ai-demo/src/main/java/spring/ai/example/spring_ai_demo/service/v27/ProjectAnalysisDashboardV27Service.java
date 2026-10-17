package spring.ai.example.spring_ai_demo.service.v27;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v27.ProjectAnalysisDashboardV27Response;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v21.OracleMetadataService;

@Service
public class ProjectAnalysisDashboardV27Service {

    private final ProjectStructureIndexService indexService;
    private final OracleMetadataService oracleMetadataService;

    public ProjectAnalysisDashboardV27Service(
            ProjectStructureIndexService indexService,
            OracleMetadataService oracleMetadataService) {
        this.indexService = indexService;
        this.oracleMetadataService = oracleMetadataService;
    }

    public ProjectAnalysisDashboardV27Response getDashboard(
            String schema) {

        var index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "Project index is not ready.");
        }

        // شمارش کلاس‌ها و فراخوانی‌های Java
        int classCount = index.classes().size();
        int methodCallCount = index.calls().size();

        // در این نسخه، شمارش اشیای Oracle از متادیتای واقعی گرفته می‌شود.
        var oracleObjects = oracleMetadataService.listObjects(
                schema, null, null);

        int oracleObjectCount = oracleObjects.size();

        String resolvedSchema =
                schema == null || schema.isBlank()
                        ? "DEFAULT"
                        : schema.trim().toUpperCase();

        return new ProjectAnalysisDashboardV27Response(
                index.projectName(),
                classCount,
                methodCallCount,
                resolvedSchema,
                oracleObjectCount,
                "STATIC_SOURCE_ANALYSIS",
                "Counts are aggregated from the Java index and Oracle metadata."
        );
    }
}