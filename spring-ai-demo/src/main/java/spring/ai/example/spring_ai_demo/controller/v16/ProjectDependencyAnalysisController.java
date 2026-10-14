package spring.ai.example.spring_ai_demo.controller.v16;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.ai.example.spring_ai_demo.service.v16.ProjectDependencyAnalysisService;
import spring.ai.example.spring_ai_demo.service.v16.ProjectDependencyAnalysisService.DependencyReport;

/**
 RequestRESTService
 */
@RestController
@RequestMapping("/api/v16/dependencies")
public class ProjectDependencyAnalysisController {

    private final ProjectDependencyAnalysisService service;

    public ProjectDependencyAnalysisController(
            ProjectDependencyAnalysisService service) {
        this.service = service;
    }

    @GetMapping("/class")
    public DependencyReport analyzeClass(
            @RequestParam String name) {
        return service.analyzeClass(name);
    }
}

/**
 {
 "projectName": "j2ee-pension-proj",
 "className": "ir.tamin.insurance.centralizedpension.ws.rest.RequestRESTService",
 "classKind": "CLASS",
 "sourcePath": "j2ee-pension-proj/centralized-pension-web/src/main/java/ir/tamin/insurance/centralizedpension/ws/rest/RequestRESTService.java",
 "imports": [
 "javax.annotation.security.RolesAllowed",
 "javax.inject.Inject",
 "javax.inject.Named",
 "javax.persistence.EntityManager",
 "javax.persistence.NoResultException",
 "javax.ws.rs.*",
 "javax.ws.rs.core.Context",
 "javax.ws.rs.core.MediaType",
 "javax.ws.rs.core.Response",
 "javax.ws.rs.core.SecurityContext",
 "java.math.BigDecimal",
 "java.util.Comparator",
 "com.ghasemkiani.util.icu.PersianDateFormat",
 "ir.tamin.framework.ws.rest.security.jwt.annotation.Secured",
 "ir.tamin.framework.domain.proxy.ProxyProcessingException",
 "ir.tamin.framework.ws.rest.ResponseHelper",
 "ir.tamin.framework.ws.rest.proxy.DBFunctionProxyV2",
 "ir.tamin.insurance.centralizedpension.function.calcRequest29.CalcRequest29Input",
 "ir.tamin.insurance.centralizedpension.function.calcRequest29.FnCalcRequest29",
 "ir.tamin.insurance.centralizedpension.model.Process",
 "ir.tamin.insurance.centralizedpension.model.baseinfo.registration.InsuranceSpec",
 "ir.tamin.insurance.centralizedpension.model.edict.request.Request",
 "ir.tamin.insurance.centralizedpension.model.edict.request.Request29",
 "ir.tamin.insurance.centralizedpension.model.edict.request.RequestType",
 "ir.tamin.insurance.centralizedpension.model.edict.requestsetting.RequestTypeStep",
 "ir.tamin.insurance.centralizedpension.model.edict.requestsetting.Step",
 "ir.tamin.insurance.centralizedpension.model.harmful.ClsuMinutes",
 "ir.tamin.insurance.centralizedpension.model.harmful.ProcessDetail",
 "ir.tamin.insurance.centralizedpension.model.harmful.ProcessHead",
 "ir.tamin.insurance.centralizedpension.model.inquiry.history.FnVwhisLsupKarnamehValue",
 "ir.tamin.insurance.centralizedpension.model.inquiry.history.HistoryInfo",
 "ir.tamin.insurance.centralizedpension.service.edict.request.RequestService",
 "ir.tamin.insurance.centralizedpension.service.edict.request.executor.MarkStepExecutor",
 "ir.tamin.insurance.centralizedpension.service.registration.IdentityInfoService",
 "ir.tamin.insurance.centralizedpension.service.report.HistoryReportService",
 "ir.tamin.insurance.centralizedpension.service.ws.HarmfulService",
 "org.bouncycastle.ocsp.Req",
 "java.util.ArrayList",
 "java.util.HashMap",
 "java.util.List",
 "java.util.Map",
 "java.util.logging.Level",
 "java.util.stream.IntStream"
 ],
 "outgoingClasses": [
 "ir.tamin.insurance.centralizedpension.function.calcRequest29.CalcRequest29Input",
 "ir.tamin.insurance.centralizedpension.function.calcRequest29.FnCalcRequest29",
 "ir.tamin.insurance.centralizedpension.model.Process",
 "ir.tamin.insurance.centralizedpension.model.baseinfo.registration.InsuranceSpec",
 "ir.tamin.insurance.centralizedpension.model.edict.request.Request",
 "ir.tamin.insurance.centralizedpension.model.edict.request.Request29",
 "ir.tamin.insurance.centralizedpension.model.edict.request.RequestType",
 "ir.tamin.insurance.centralizedpension.model.edict.requestsetting.RequestTypeStep",
 "ir.tamin.insurance.centralizedpension.model.edict.requestsetting.Step",
 "ir.tamin.insurance.centralizedpension.model.harmful.ClsuMinutes",
 "ir.tamin.insurance.centralizedpension.model.harmful.ProcessDetail",
 "ir.tamin.insurance.centralizedpension.model.harmful.ProcessHead",
 "ir.tamin.insurance.centralizedpension.model.inquiry.history.FnVwhisLsupKarnamehValue",
 "ir.tamin.insurance.centralizedpension.model.inquiry.history.HistoryInfo",
 "ir.tamin.insurance.centralizedpension.service.edict.request.RequestService",
 "ir.tamin.insurance.centralizedpension.service.edict.request.executor.MarkStepExecutor",
 "ir.tamin.insurance.centralizedpension.service.registration.IdentityInfoService",
 "ir.tamin.insurance.centralizedpension.service.report.HistoryReportService",
 "ir.tamin.insurance.centralizedpension.service.ws.HarmfulService"
 ],
 "outgoingMethodCallCount": 173,
 "declaredMethodCount": 3,
 "possibleIncomingCallCount": 0,
 "possibleIncomingCalls": [],
 "incomingResultsTruncated": false
 }
 */