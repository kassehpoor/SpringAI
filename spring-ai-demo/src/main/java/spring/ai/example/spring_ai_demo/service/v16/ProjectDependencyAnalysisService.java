package spring.ai.example.spring_ai_demo.service.v16;



import org.springframework.stereotype.Service;

import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ClassInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.MethodCallInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProjectDependencyAnalysisService {

    private final ProjectStructureIndexService indexService;

    public ProjectDependencyAnalysisService(
            ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    public DependencyReport analyzeClass(String className) {
        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException(
                    "ابتدا پروژه را در V12 نمایه‌سازی کنید.");
        }

        ClassInfo target = index.classes().values().stream()
                .filter(c -> c.qualifiedName().equals(className)
                        || c.name().equals(className))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "کلاس پیدا نشد: " + className));

        Set<String> declaredMethods = new LinkedHashSet<>();

        Pattern methodPattern = Pattern.compile(
                "\\b([A-Za-z_$][\\w$]*)\\s*\\(");

        for (String declaration : target.methods()) {
            Matcher matcher = methodPattern.matcher(declaration);

            String lastName = null;
            while (matcher.find()) {
                lastName = matcher.group(1);
            }

            if (lastName != null) {
                declaredMethods.add(lastName);
            }
        }

        Set<String> outgoingClasses = new LinkedHashSet<>();

        // وابستگی‌های احتمالی از روی importها
        for (String imported : target.imports()) {
            String normalized = imported.replace(".*", "");

            for (ClassInfo candidate : index.classes().values()) {
                if (candidate.qualifiedName().equals(normalized)
                        || candidate.qualifiedName().startsWith(
                        normalized + ".")) {
                    outgoingClasses.add(candidate.qualifiedName());
                }
            }
        }

        List<MethodCallInfo> outgoingCalls = index.calls().stream()
                .filter(call -> target.qualifiedName()
                        .equals(call.sourceClass()))
                .toList();

        // وابستگی‌های احتمالی از روی scope فراخوانی متدها
        for (MethodCallInfo call : outgoingCalls) {
            String scope = call.scope();

            if (scope == null || scope.isBlank()
                    || scope.equals("this") || scope.equals("super")) {
                continue;
            }

            String normalizedScope = scope.replace("this.", "");

            for (ClassInfo candidate : index.classes().values()) {
                if (candidate.name().equals(normalizedScope)
                        || candidate.qualifiedName().equals(normalizedScope)) {
                    outgoingClasses.add(candidate.qualifiedName());
                }
            }
        }

        // فراخوانی‌هایی که نام متد آن‌ها با متدهای کلاس هدف یکسان است.
        // این تطبیق نام به‌تنهایی اثبات نمی‌کند مقصد واقعی همان کلاس است.
        List<MethodCallInfo> possibleIncomingCalls = index.calls().stream()
                .filter(call -> !target.qualifiedName()
                        .equals(call.sourceClass()))
                .filter(call -> declaredMethods.contains(call.methodName()))
                .toList();

        List<IncomingCall> incoming = possibleIncomingCalls.stream()
                .limit(200)
                .map(call -> new IncomingCall(
                        call.sourceClass(),
                        call.sourceMethod(),
                        call.methodName(),
                        call.path(),
                        call.line(),
                        "NAME_MATCH_ONLY"
                ))
                .toList();

        return new DependencyReport(
                index.projectName(),
                target.qualifiedName(),
                target.kind(),
                target.path(),
                target.imports(),
                new ArrayList<>(outgoingClasses),
                outgoingCalls.size(),
                declaredMethods.size(),
                possibleIncomingCalls.size(),
                incoming,
                possibleIncomingCalls.size() > 200
        );
    }

    public record DependencyReport(
            String projectName,
            String className,
            String classKind,
            String sourcePath,
            List<String> imports,
            List<String> outgoingClasses,
            int outgoingMethodCallCount,
            int declaredMethodCount,
            int possibleIncomingCallCount,
            List<IncomingCall> possibleIncomingCalls,
            boolean incomingResultsTruncated
    ) {}

    public record IncomingCall(
            String sourceClass,
            String sourceMethod,
            String calledMethod,
            String path,
            int line,
            String matchType
    ) {}
}
