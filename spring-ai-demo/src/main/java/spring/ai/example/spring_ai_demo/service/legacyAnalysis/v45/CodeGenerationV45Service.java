package spring.ai.example.spring_ai_demo.service.legacyAnalysis.v45;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
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


        String rawResponse = chatClient.prompt()
                .system(systemPrompt + """

        IMPORTANT OUTPUT RULES:
        - Return exactly one valid JSON object.
        - Do not include Markdown or explanations.
        - Use exactly these JSON fields:
          projectName, packageName, files, assumptions, reviewNotes.
        - The files field must contain at least one Java file object.
        - Each file object must have these fields:
          path, fileName, type, content.
        - Every file path must end with .java.
        - Put the complete Java source code inside content.
        - Use jakarta.validation, not javax.validation.
        - Return valid JSON with properly escaped newlines and quotes.
        """)
                .user(userPrompt)
                .options(OllamaOptions.builder()
                        .format("json")
                        .build())
                .call()
                .content();

        System.out.println("========== V45 RAW RESPONSE ==========");
        System.out.println(rawResponse);
        System.out.println("========== END V45 RAW RESPONSE ======");

        GeneratedCodeV45DTO generated;

        try {
            generated = objectMapper.readValue(
                    rawResponse,
                    GeneratedCodeV45DTO.class
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "پاسخ Ollama یک JSON معتبر نیست. پاسخ خام: "
                            + (rawResponse == null
                            ? "null"
                            : rawResponse.substring(
                            0,
                            Math.min(1500, rawResponse.length())
                    )),
                    e
            );
        }


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
            if (file == null) {
                throw new IllegalStateException(
                        "مدل زبانی یک فایل null تولید کرده است.");
            }

            boolean invalidPath = file.path() == null
                    || !file.path().endsWith(".java")
                    || file.path().startsWith("/")
                    || file.path().contains("..");

            boolean invalidContent = file.content() == null
                    || !file.content().contains("package ");

            if (invalidPath || invalidContent) {
                System.err.println("=== V45 INVALID GENERATED FILE ===");
                System.err.println("path = " + file.path());
                System.err.println("content is null = " + (file.content() == null));

                if (file.content() != null) {
                    System.err.println("content preview = "
                            + file.content().substring(
                            0, Math.min(500, file.content().length())));
                }

                throw new IllegalStateException(
                        "ساختار فایل تولیدشده معتبر نیست؛ جزئیات در لاگ ثبت شد.");
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