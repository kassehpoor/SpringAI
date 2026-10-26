package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v45;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v45.GeneratedCodeV45DTO;
import spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v44.DataModelProposalV44Repository;
import spring.ai.example.spring_ai_demo.repository.legacyAnalysis.v45.GeneratedCodeV45Repository;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CodeGenerationV45Service {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final DataModelProposalV44Repository proposalRepository;
    private final GeneratedCodeV45Repository codeRepository;
    private final String modelName;

    public CodeGenerationV45Service(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            DataModelProposalV44Repository proposalRepository,
            GeneratedCodeV45Repository codeRepository,
            @Value("${spring.ai.ollama.chat.options.model:llama3.2}")
            String modelName) {

        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.proposalRepository = proposalRepository;
        this.codeRepository = codeRepository;
        this.modelName = modelName;
    }

    @Transactional
    public Map<String, Object> generate(
            Long proposalId,
            String packageName) {

        if (proposalId == null || proposalId <= 0) {
            throw new IllegalArgumentException("proposalId معتبر الزامی است.");
        }

        if (packageName == null
                || !packageName.matches(
                "[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*")) {
            throw new IllegalArgumentException(
                    "packageName معتبر نیست.");
        }

        Map<String, Object> storedProposal =
                proposalRepository.findById(proposalId);

        String proposalJson =
                String.valueOf(storedProposal.get("proposal_json"));

        String systemPrompt = """
                You are a senior Java and Spring Boot architect.

                Generate a reviewable Java code draft from the supplied
                approved-or-draft data model proposal.

                Requirements:
                - Use Java 21 and Spring Boot 3 conventions.
                - Generate only relevant Entity, DTO, Repository,
                  Service, Controller and focused tests.
                - Use Jakarta Persistence annotations for entities.
                - Use constructor injection.
                - Do not invent business rules that are not in the proposal.
                - Mark uncertain assumptions in assumptions and reviewNotes.
                - Do not generate database credentials, destructive SQL,
                  production deployment scripts, or code that executes itself.
                - Do not claim the code compiles unless verified.
                - Keep the output limited to at most 10 Java files.
                - Each file path must be relative and end in .java.
                - Each content value must contain the complete Java source,
                  including its package declaration and imports.
                - Return a structured response matching the requested DTO.
                """;

        String userPrompt = """
                Model: %s
                Base package: %s

                Data model proposal JSON:
                %s

                Generate the first coherent implementation draft.
                Use package subdirectories appropriate to each class.
                """.formatted(modelName, packageName, proposalJson);

        GeneratedCodeV45DTO generated = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(GeneratedCodeV45DTO.class);

        if (generated == null || generated.files() == null
                || generated.files().isEmpty()) {
            throw new IllegalStateException(
                    "مدل زبانی هیچ فایل کدی تولید نکرد.");
        }

        if (generated.files().size() > 10) {
            throw new IllegalStateException(
                    "تعداد فایل‌های خروجی بیش از حد مجاز است.");
        }

        // کنترل حداقلی خروجی؛ هیچ فایل روی دیسک اجرا یا نوشته نمی‌شود.
        for (GeneratedCodeV45DTO.JavaFile file : generated.files()) {
            if (file.path() == null
                    || !file.path().endsWith(".java")
                    || file.path().startsWith("/")
                    || file.path().contains("..")
                    || file.content() == null
                    || !file.content().contains("package ")) {
                throw new IllegalStateException(
                        "ساختار یکی از فایل‌های تولیدشده معتبر نیست.");
            }
        }

        final String filesJson;
        try {
            filesJson = objectMapper.writeValueAsString(generated);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "تبدیل کدهای تولیدشده به JSON ناموفق بود.", e);
        }

        Long generationId = codeRepository.save(
                proposalId,
                packageName,
                filesJson
        );

        return Map.of(
                "generationId", generationId,
                "proposalId", proposalId,
                "packageName", packageName,
                "fileCount", generated.files().size(),
                "status", "DRAFT",
                "message", "کدها تولید و ذخیره شدند؛ هنوز کامپایل یا تأیید نشده‌اند."
        );
    }

    public Map<String, Object> getGeneratedCode(Long id) {
        return codeRepository.findById(id);
    }

    public List<Map<String, Object>> listByProposal(Long proposalId) {
        return codeRepository.findByProposalId(proposalId);
    }
}