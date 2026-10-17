package spring.ai.example.spring_ai_demo.service.v11;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v11.ProjectAnalysisResponse;
import spring.ai.example.spring_ai_demo.dto.v11.ProjectAnalysisResponse.JavaFileSummary;
import spring.ai.example.spring_ai_demo.dto.v11.ProjectAnalysisResponse.TypeSummary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;



/**
 این نسخه اطلاعات ساختاری را استخراج می‌کند؛ هنوز گراف کامل وابستگی متدها، وراثت بین کلاس‌ها یا مسیرهای فراخوانی را محاسبه نمی‌کند. همچنین این محدودیت‌ها برای شروع مناسب‌اند، اما برای استفاده سازمانی باید مدیریت خطاها و محدودیت منابع را کامل‌تر کنیم.
 */

@Service
public class ProjectZipAnalysisService {

    private static final int MAX_ENTRIES = 10_000;
    private static final int MAX_JAVA_FILE_BYTES = 2 * 1024 * 1024;
    private static final long MAX_TOTAL_BYTES = 30L * 1024 * 1024;

    private final JavaParser javaParser;

    public ProjectZipAnalysisService() {
        ParserConfiguration configuration =
                new ParserConfiguration()
                        .setLanguageLevel(
                                ParserConfiguration.LanguageLevel.JAVA_21
                        );

        this.javaParser = new JavaParser(configuration);
    }

    public ProjectAnalysisResponse analyze(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "فایل ZIP الزامی است."
            );
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null
                || !originalName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new IllegalArgumentException(
                    "فقط فایل با پسوند zip پذیرفته می‌شود."
            );
        }

        List<JavaFileSummary> summaries = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int javaFileCount = 0;
        int parsedFileCount = 0;
        int failedFileCount = 0;
        int entryCount = 0;
        long totalBytes = 0;

        try (InputStream input = file.getInputStream();
             ZipInputStream zip = new ZipInputStream(input)) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                entryCount++;

                if (entryCount > MAX_ENTRIES) {
                    throw new IllegalArgumentException(
                            "تعداد فایل‌های ZIP از حد مجاز بیشتر است."
                    );
                }

                if (entry.isDirectory()) {
                    zip.closeEntry();
                    continue;
                }

                String path = entry.getName().replace('\\', '/');

                // جلوگیری از پردازش مسیرهای نامعتبر
                if (path.startsWith("/")
                        || path.matches("^[A-Za-z]:.*")
                        || List.of(path.split("/")).contains("..")) {
                    errors.add("مسیر نامعتبر نادیده گرفته شد.");
                    zip.closeEntry();
                    continue;
                }

                if (!path.toLowerCase(Locale.ROOT).endsWith(".java")) {
                    zip.closeEntry();
                    continue;
                }

                javaFileCount++;

                byte[] sourceBytes = readEntry(
                        zip,
                        MAX_JAVA_FILE_BYTES
                );

                totalBytes += sourceBytes.length;

                if (totalBytes > MAX_TOTAL_BYTES) {
                    throw new IllegalArgumentException(
                            "حجم کل فایل‌های Java از حد مجاز بیشتر است."
                    );
                }

                String source = new String(
                        sourceBytes,
                        StandardCharsets.UTF_8
                );

                try {
                    ParseResult<CompilationUnit> parseResult =
                            javaParser.parse(source);

                    if (parseResult.getResult().isEmpty()
                            || !parseResult.getProblems().isEmpty()) {

                        failedFileCount++;
                        errors.add(
                                path + ": خطا در تحلیل نحوی Java"
                        );
                        zip.closeEntry();
                        continue;
                    }

                    CompilationUnit unit =
                            parseResult.getResult().orElseThrow();

                    List<String> imports = unit.getImports()
                            .stream()
                            .map(Object::toString)
                            .toList();

                    List<TypeSummary> types = unit.getTypes()
                            .stream()
                            .map(this::toTypeSummary)
                            .toList();

                    summaries.add(
                            new JavaFileSummary(
                                    path,
                                    unit.getPackageDeclaration()
                                            .map(pkg -> pkg.getNameAsString())
                                            .orElse(""),
                                    imports,
                                    types
                            )
                    );

                    parsedFileCount++;

                } catch (RuntimeException exception) {
                    failedFileCount++;
                    errors.add(
                            path + ": "
                                    + exception.getClass().getSimpleName()
                    );
                }

                zip.closeEntry();
            }
        }

        String projectName = originalName.substring(
                0,
                originalName.length() - ".zip".length()
        );

        return new ProjectAnalysisResponse(
                projectName,
                javaFileCount,
                parsedFileCount,
                failedFileCount,
                summaries,
                errors
        );
    }

    private TypeSummary toTypeSummary(TypeDeclaration<?> type) {

        String kind;

        if (type.isClassOrInterfaceDeclaration()) {
            kind = type.asClassOrInterfaceDeclaration().isInterface()
                    ? "INTERFACE"
                    : "CLASS";
        } else if (type.isEnumDeclaration()) {
            kind = "ENUM";
        } else if (type.isRecordDeclaration()) {
            kind = "RECORD";
        } else {
            kind = "OTHER";
        }

        List<String> methods = type.getMembers()
                .stream()
                .filter(MethodDeclaration.class::isInstance)
                .map(MethodDeclaration.class::cast)
                .map(MethodDeclaration::getDeclarationAsString)
                .toList();

        return new TypeSummary(
                type.getNameAsString(),
                kind,
                methods
        );
    }

    private byte[] readEntry(
            InputStream input,
            int maxBytes) throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        int total = 0;

        while ((count = input.read(buffer)) != -1) {
            total += count;

            if (total > maxBytes) {
                throw new IllegalArgumentException(
                        "حجم یک فایل Java از حد مجاز بیشتر است."
                );
            }

            output.write(buffer, 0, count);
        }

        return output.toByteArray();
    }
}
