package spring.ai.example.spring_ai_demo.controller.v18;




import spring.ai.example.spring_ai_demo.service.v18.LayerDependencyAnalysisService;
import spring.ai.example.spring_ai_demo.service.v18.LayerDependencyAnalysisService.LayerReport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v18/dependencies")
public class LayerDependencyController {

    private final LayerDependencyAnalysisService service;

    public LayerDependencyController(
            LayerDependencyAnalysisService service) {
        this.service = service;
    }

    @GetMapping("/layers")
    public LayerReport analyze(
            @RequestParam("name") String className,
            @RequestParam(defaultValue = "100") int limit) {

        return service.analyze(className, limit);
    }
}
/**
 curl -X 'GET' \
 'http://localhost:9999/api/v18/dependencies/layers?name=RequestRESTService&limit=100' \
 -H 'accept:
 */


/**
 {
 "projectName": "j2ee-pension-proj",
 "analyzedClass": "ir.tamin.insurance.centralizedpension.ws.rest.RequestRESTService",
 "analyzedLayer": "REST",
 "totalDependenciesFound": 12,
 "returnedDependencies": 6,
 "truncated": false,
 "dependencies": [
 {
 "sourceClass": "ir.tamin.insurance.centralizedpension.ws.rest.RequestRESTService",
 "targetClass": "ir.tamin.insurance.centralizedpension.service.report.HistoryReportService",
 "sourceLayer": "REST",
 "targetLayer": "SERVICE",
 "evidence": "FIELD_TYPE",
 "confidence": "HIGH"
 },
 {
 "sourceClass": "ir.tamin.insurance.centralizedpension.ws.rest.RequestRESTService",
 "targetClass": "ir.tamin.insurance.centralizedpension.service.registration.IdentityInfoService",
 "sourceLayer": "REST",
 "targetLayer": "SERVICE",
 "evidence": "FIELD_TYPE",
 "confidence": "HIGH"
 },
 {
 "sourceClass": "ir.tamin.insurance.centralizedpension.ws.rest.RequestRESTService",
 "targetClass": "ir.tamin.insurance.centralizedpension.service.edict.request.executor.MarkStepExecutor",
 "sourceLayer": "REST",
 "targetLayer": "SERVICE",
 "evidence": "FIELD_TYPE",
 "confidence": "HIGH"
 },
 {
 "sourceClass": "ir.tamin.insurance.centralizedpension.ws.rest.RequestRESTService",
 "targetClass": "ir.tamin.insurance.centralizedpension.service.edict.request.RequestService",
 "sourceLayer": "REST",
 "targetLayer": "SERVICE",
 "evidence": "FIELD_TYPE",
 "confidence": "HIGH"
 },
 {
 "sourceClass": "ir.tamin.insurance.centralizedpension.ws.rest.RequestRESTService",
 "targetClass": "ir.tamin.insurance.centralizedpension.service.ws.HarmfulService",
 "sourceLayer": "REST",
 "targetLayer": "SERVICE",
 "evidence": "FIELD_TYPE",
 "confidence": "HIGH"
 },
 {
 "sourceClass": "ir.tamin.insurance.centralizedpension.ws.rest.RequestRESTService",
 "targetClass": "ir.tamin.insurance.centralizedpension.ws.rest.RequestRESTService",
 "sourceLayer": "REST",
 "targetLayer": "REST",
 "evidence": "METHOD_SCOPE",
 "confidence": "MEDIUM"
 }
 ],
 "note": "Static source analysis only; dependencies do not prove runtime execution."
 }
 */