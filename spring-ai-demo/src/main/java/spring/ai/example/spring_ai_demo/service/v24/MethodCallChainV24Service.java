package spring.ai.example.spring_ai_demo.service.v24;

import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v24.MethodCallChainResponse;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.MethodCallInfo;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService.ProjectIndex;

import java.util.ArrayList;
import java.util.List;

@Service
public class MethodCallChainV24Service {

    private final ProjectStructureIndexService indexService;

    public MethodCallChainV24Service(ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    public MethodCallChainResponse findChain(
            String sourceClass,
            String sourceMethod,
            int maxDepth) {

        ProjectIndex index = indexService.getCurrentIndex();

        if (index == null) {
            throw new IllegalStateException("ایندکس پروژه آماده نیست.");
        }

        int depthLimit = Math.max(1, Math.min(maxDepth, 10));

        List<MethodCallInfo> allCalls = index.calls();

        List<MethodCallChainResponse.CallStep> chain =
                new ArrayList<>();

        collectCalls(
                allCalls,
                sourceClass,
                sourceMethod,
                depthLimit,
                chain,
                new ArrayList<>()
        );

        return new MethodCallChainResponse(
                index.projectName(),
                sourceClass,
                sourceMethod,
                chain,
                chain.size()
        );
    }

    private void collectCalls(
            List<MethodCallInfo> allCalls,
            String currentClass,
            String currentMethod,
            int remainingDepth,
            List<MethodCallChainResponse.CallStep> result,
            List<String> visited) {

        if (remainingDepth <= 0) {
            return;
        }

        String currentKey = currentClass + "#" + currentMethod;

        if (visited.contains(currentKey)) {
            return;
        }

        List<String> nextVisited = new ArrayList<>(visited);
        nextVisited.add(currentKey);

        for (MethodCallInfo call : allCalls) {

            if (!currentClass.equals(call.sourceClass())) {
                continue;
            }

            if (!currentMethod.equals(call.sourceMethod())) {
                continue;
            }

            result.add(new MethodCallChainResponse.CallStep(
                    call.sourceClass(),
                    call.methodName(),
                    call.path(),
                    call.line(),
                    call.scope()
            ));
        }
    }
}