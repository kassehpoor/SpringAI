
package spring.ai.example.spring_ai_demo.service.v12;

/**
 * @author Tahereh Kasehpoor
 */

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import spring.ai.example.spring_ai_demo.dto.v12.ProjectIndexResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProjectStructureIndexService {

    private static final int MAX_ENTRIES = 10_000;
    private static final int MAX_FILE_BYTES = 2 * 1024 * 1024;
    private static final long MAX_TOTAL_BYTES = 100L * 1024 * 1024;

    private final JavaParser parser;

    private volatile ProjectIndex currentIndex;

    public ProjectStructureIndexService() {
        ParserConfiguration configuration =
                new ParserConfiguration()
                        .setLanguageLevel(
                                ParserConfiguration.LanguageLevel.JAVA_21
                        );

        this.parser = new JavaParser(configuration);
    }

    public synchronized ProjectIndexResponse buildIndex(
            MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("فایل ZIP الزامی است.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null
                || !originalName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new IllegalArgumentException(
                    "فقط فایل ZIP پذیرفته می‌شود."
            );
        }

        Map<String, ClassInfo> classes = new LinkedHashMap<>();
        List<MethodCallInfo> calls = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // V14: نگهداری متن کامل فایل‌های Java برای تحلیل SQL و Oracle
        Map<String, String> sourceFiles = new LinkedHashMap<>();

        int javaFileCount = 0;
        int parsedFileCount = 0;
        int failedFileCount = 0;
        long totalBytes = 0;
        int entryCount = 0;

        try (InputStream input = file.getInputStream();
             ZipInputStream zip = new ZipInputStream(input)) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                entryCount++;

                if (entryCount > MAX_ENTRIES) {
                    throw new IllegalArgumentException(
                            "تعداد ورودی‌های ZIP بیش از حد مجاز است."
                    );
                }

                if (entry.isDirectory()) {
                    zip.closeEntry();
                    continue;
                }

                String path = entry.getName().replace('\\', '/');

                if (path.startsWith("/")
                        || path.matches("^[A-Za-z]:.*")
                        || List.of(path.split("/")).contains("..")) {
                    zip.closeEntry();
                    continue;
                }

                if (!path.toLowerCase(Locale.ROOT).endsWith(".java")) {
                    zip.closeEntry();
                    continue;
                }

                javaFileCount++;

                byte[] bytes = readEntry(zip, MAX_FILE_BYTES);
                totalBytes += bytes.length;

                if (totalBytes > MAX_TOTAL_BYTES) {
                    throw new IllegalArgumentException(
                            "حجم کل فایل‌های Java بیش از حد مجاز است."
                    );
                }

                String source = new String(
                        bytes,
                        StandardCharsets.UTF_8
                );

                // متن را حتی در صورت شکست Parse نگه می‌داریم
                // تا V14 بتواند SQLهای فایل را نیز بررسی کند.
                sourceFiles.put(path, source);

                try {
                    ParseResult<CompilationUnit> parsed =
                            parser.parse(source);

                    if (parsed.getResult().isEmpty()
                            || !parsed.getProblems().isEmpty()) {
                        failedFileCount++;
                        errors.add(path + ": خطای parse");
                        zip.closeEntry();
                        continue;
                    }

                    CompilationUnit unit =
                            parsed.getResult().orElseThrow();

                    String packageName = unit.getPackageDeclaration()
                            .map(p -> p.getNameAsString())
                            .orElse("");

                    List<String> imports = unit.getImports()
                            .stream()
                            .map(i -> i.getNameAsString()
                                    + (i.isAsterisk() ? ".*" : ""))
                            .toList();

                    List<TypeDeclaration<?>> types = unit.getTypes();

                    for (TypeDeclaration<?> type : types) {
                        ClassInfo info = toClassInfo(
                                path,
                                packageName,
                                imports,
                                type
                        );

                        classes.put(info.qualifiedName(), info);
                    }

                    for (MethodCallExpr call :
                            unit.findAll(MethodCallExpr.class)) {

                        Optional<MethodDeclaration> enclosingMethod =
                                call.findAncestor(MethodDeclaration.class);

                        Optional<TypeDeclaration> enclosingType =
                                call.findAncestor(TypeDeclaration.class);

                        String sourceClass = enclosingType
                                .map(t -> qualifiedName(
                                        packageName,
                                        t.getNameAsString()
                                ))
                                .orElse("");

                        String sourceMethod = enclosingMethod
                                .map(MethodDeclaration::getDeclarationAsString)
                                .orElse("<initializer-or-field>");

                        String scope = call.getScope()
                                .map(Object::toString)
                                .orElse("");

                        int line = call.getBegin()
                                .map(p -> p.line)
                                .orElse(-1);

                        calls.add(new MethodCallInfo(
                                path,
                                sourceClass,
                                sourceMethod,
                                call.getNameAsString(),
                                scope,
                                line
                        ));
                    }

                    parsedFileCount++;

                } catch (RuntimeException exception) {
                    failedFileCount++;
                    errors.add(path + ": خطای تحلیل ساختاری");
                }

                zip.closeEntry();
            }
        }

        int methodCount = classes.values().stream()
                .mapToInt(c -> c.methods().size())
                .sum();

        String projectName = originalName.substring(
                0,
                originalName.length() - ".zip".length()
        );

        currentIndex = new ProjectIndex(
                projectName,
                classes,
                calls,
                errors,
                sourceFiles
        );

        return new ProjectIndexResponse(
                projectName,
                javaFileCount,
                parsedFileCount,
                classes.size(),
                methodCount,
                calls.size(),
                failedFileCount,
                "نمایه ساختاری و متن فایل‌های Java در حافظه ذخیره شد."
        );
    }

    private ClassInfo toClassInfo(
            String path,
            String packageName,
            List<String> imports,
            TypeDeclaration<?> type) {

        String kind = "OTHER";
        List<String> inheritance = new ArrayList<>();

        if (type instanceof ClassOrInterfaceDeclaration declaration) {
            kind = declaration.isInterface() ? "INTERFACE" : "CLASS";

            declaration.getExtendedTypes()
                    .forEach(t -> inheritance.add("extends " + t));

            declaration.getImplementedTypes()
                    .forEach(t -> inheritance.add("implements " + t));

        } else if (type instanceof EnumDeclaration) {
            kind = "ENUM";

        } else if (type instanceof RecordDeclaration) {
            kind = "RECORD";

        } else if (type instanceof AnnotationDeclaration) {
            kind = "ANNOTATION";
        }

        List<String> methods = type.getMembers().stream()
                .filter(MethodDeclaration.class::isInstance)
                .map(MethodDeclaration.class::cast)
                .map(MethodDeclaration::getDeclarationAsString)
                .toList();

        String qualifiedName = qualifiedName(
                packageName,
                type.getNameAsString()
        );

        return new ClassInfo(
                qualifiedName,
                type.getNameAsString(),
                kind,
                path,
                packageName,
                imports,
                inheritance,
                methods
        );
    }

    private String qualifiedName(
            String packageName,
            String typeName) {

        return packageName == null || packageName.isBlank()
                ? typeName
                : packageName + "." + typeName;
    }

    private byte[] readEntry(
            InputStream input,
            int maxBytes) throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;

        while ((count = input.read(buffer)) != -1) {
            if (output.size() + count > maxBytes) {
                throw new IllegalArgumentException(
                        "یک فایل Java از حد مجاز بزرگ‌تر است."
                );
            }

            output.write(buffer, 0, count);
        }

        return output.toByteArray();
    }

    public ProjectIndex getCurrentIndex() {
        return currentIndex;
    }

    public record ClassInfo(
            String qualifiedName,
            String name,
            String kind,
            String path,
            String packageName,
            List<String> imports,
            List<String> inheritance,
            List<String> methods
    ) {}

    public record MethodCallInfo(
            String path,
            String sourceClass,
            String sourceMethod,
            String methodName,
            String scope,
            int line
    ) {}

    public record ProjectIndex(
            String projectName,
            Map<String, ClassInfo> classes,
            List<MethodCallInfo> calls,
            List<String> errors,
            Map<String, String> sourceFiles
    ) {}
}
