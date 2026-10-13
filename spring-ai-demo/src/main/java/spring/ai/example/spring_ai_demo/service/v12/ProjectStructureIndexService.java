
package spring.ai.example.spring_ai_demo.service.v12;

/**
 * @author Tahereh Kasehpoor
 */

import com.fasterxml.jackson.databind.ObjectMapper;
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

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import spring.ai.example.spring_ai_demo.config.ProjectIndexProperties;
import spring.ai.example.spring_ai_demo.dto.v12.ProjectIndexResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProjectStructureIndexService {

    private static final int MAX_ENTRIES = 10_000;
    private static final int MAX_FILE_BYTES = 2 * 1024 * 1024;
    private static final long MAX_TOTAL_BYTES = 100L * 1024 * 1024;

    private final JavaParser parser;
    private final Path storageRoot;
    private final Path projectsRoot;
    private final Path currentProjectFile;

    private volatile ProjectIndex currentIndex;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProjectStructureIndexService(
            ProjectIndexProperties properties) {

        ParserConfiguration configuration =
                new ParserConfiguration()
                        .setLanguageLevel(
                                ParserConfiguration.LanguageLevel.JAVA_21
                        );

        this.parser = new JavaParser(configuration);
        this.storageRoot = Path.of(properties.storageDir())
                .toAbsolutePath().normalize();
        this.projectsRoot = storageRoot.resolve("projects");
        this.currentProjectFile = storageRoot.resolve("current-project.txt");
    }


    private Path getIndexFile(String projectName) {
        return storageRoot
                .resolve("projects")
                .resolve(projectName)
                .resolve("index.json");
    }

    /**
     * پس از بالا آمدن برنامه، نمایه آخرین پروژه را از دیسک بازیابی می‌کند.
     */
    @PostConstruct
    public void restoreLastProject() {
        try {
            Files.createDirectories(projectsRoot);

            if (!Files.exists(currentProjectFile)) {
                return;
            }

            String projectName = Files.readString(
                    currentProjectFile,
                    StandardCharsets.UTF_8
            ).trim();

            if (projectName.isBlank()
                    || !projectName.matches("[A-Za-z0-9._-]+")) {
                return;
            }

            Path projectDirectory = projectsRoot.resolve(projectName)
                    .toAbsolutePath().normalize();
            Path normalizedProjectsRoot = projectsRoot.toAbsolutePath().normalize();
            Path sourceDirectory = projectDirectory.resolve("source").normalize();

            if (!projectDirectory.startsWith(normalizedProjectsRoot)
                    || !sourceDirectory.startsWith(projectDirectory)
                    || !Files.isDirectory(sourceDirectory)) {
                return;
            }

            // ابتدا نمایه ذخیره‌شده را بارگذاری می‌کنیم؛ فقط اگر وجود نداشت، یک‌بار Parse می‌کنیم.
            currentIndex = loadIndex(projectName);
            if (currentIndex == null) {
                currentIndex = createIndex(projectName, sourceDirectory);
                saveIndex(currentIndex);
            }

            System.out.println("نمایه پروژه بازیابی شد: " + projectName);

        } catch (Exception exception) {
            // برنامه بالا می‌آید؛ در صورت خطا، ZIP دوباره قابل بارگذاری است.
            currentIndex = null;
        }
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

        String fileName = Path.of(originalName).getFileName().toString();
        String projectName = fileName.substring(0, fileName.length() - 4)
                .replaceAll("[^A-Za-z0-9._-]", "_");

        if (projectName.isBlank()) {
            throw new IllegalArgumentException("نام پروژه معتبر نیست.");
        }

        Files.createDirectories(projectsRoot);

        Path projectDirectory = projectsRoot.resolve(projectName).normalize();
        Path sourceDirectory = projectDirectory.resolve("source").normalize();

        if (!sourceDirectory.startsWith(projectsRoot)) {
            throw new IllegalArgumentException("مسیر پروژه معتبر نیست.");
        }

        // سورس قبلی همین پروژه را جایگزین می‌کنیم.
        deleteDirectory(projectDirectory);
        Files.createDirectories(sourceDirectory);

        int entryCount = 0;
        long totalBytes = 0;

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

                String entryName = entry.getName().replace('\\', '/');

                if (entryName.startsWith("/")
                        || entryName.matches("^[A-Za-z]:.*")
                        || List.of(entryName.split("/")).contains("..")) {
                    zip.closeEntry();
                    continue;
                }

                if (!entryName.toLowerCase(Locale.ROOT).endsWith(".java")) {
                    zip.closeEntry();
                    continue;
                }

                byte[] bytes = readEntry(zip, MAX_FILE_BYTES);
                totalBytes += bytes.length;

                if (totalBytes > MAX_TOTAL_BYTES) {
                    throw new IllegalArgumentException(
                            "حجم کل فایل‌های Java بیش از حد مجاز است."
                    );
                }

                Path destination = sourceDirectory.resolve(entryName)
                        .normalize();

                if (!destination.startsWith(sourceDirectory)) {
                    zip.closeEntry();
                    continue;
                }

                Files.createDirectories(destination.getParent());
                Files.write(destination, bytes);

                zip.closeEntry();
            }

        } catch (Exception exception) {
            // از باقی ماندن پروژه ناقص به‌عنوان پروژه فعال جلوگیری می‌کنیم.
            deleteDirectory(projectDirectory);

            if (exception instanceof IOException ioException) {
                throw ioException;
            }

            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new IOException("خطا در ذخیره ZIP.", exception);
        }

        ProjectIndex newIndex = createIndex(projectName, sourceDirectory);

        // نمایه ساختاری را جداگانه ذخیره می‌کنیم تا در Restart دوباره Parse نشود.
        saveIndex(newIndex);

        // نام پروژه فعال را ثبت می‌کنیم تا بعد از Restart بازیابی شود.
        Path temporaryMarker = storageRoot.resolve("current-project.tmp");

        Files.writeString(
                temporaryMarker,
                projectName,
                StandardCharsets.UTF_8
        );

        try {
            Files.move(
                    temporaryMarker,
                    currentProjectFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException exception) {
            Files.move(
                    temporaryMarker,
                    currentProjectFile,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        currentIndex = newIndex;

        int methodCount = newIndex.classes().values().stream()
                .mapToInt(c -> c.methods().size())
                .sum();

        return new ProjectIndexResponse(
                projectName,
                newIndex.sourceFileCount(),
                newIndex.parsedFileCount(),
                newIndex.classes().size(),
                methodCount,
                newIndex.calls().size(),
                newIndex.errors().size(),
                "سورس‌ها روی دیسک ذخیره و نمایه ساختاری ساخته شد."
        );
    }

    private void saveIndex(ProjectIndex index) {
        Path indexFile = getIndexFile(index.projectName());
        Path temporaryFile = indexFile.resolveSibling("index.json.tmp");

        try {
            Files.createDirectories(indexFile.getParent());

            ProjectIndexSnapshot snapshot = new ProjectIndexSnapshot(
                    index.projectName(),
                    index.classes(),
                    index.calls(),
                    index.errors(),
                    index.sourceDirectory().toAbsolutePath().normalize().toString(),
                    index.sourceFileCount(),
                    index.parsedFileCount()
            );

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temporaryFile.toFile(), snapshot);

            try {
                Files.move(
                        temporaryFile,
                        indexFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicMoveException) {
                Files.move(
                        temporaryFile,
                        indexFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException ignored) {
                // خطای اصلی ذخیره‌سازی مهم‌تر است.
            }
            throw new IllegalStateException("ذخیره نمایه ساختاری ناموفق بود.", exception);
        }
    }

    private ProjectIndex loadIndex(String projectName) {
        Path indexFile = getIndexFile(projectName);

        if (!Files.isRegularFile(indexFile)) {
            return null;
        }

        try {
            ProjectIndexSnapshot snapshot = objectMapper.readValue(
                    indexFile.toFile(), ProjectIndexSnapshot.class
            );

            if (!projectName.equals(snapshot.projectName())) {
                throw new IOException("نام پروژه در فایل نمایه با نام پروژه فعال مطابقت ندارد.");
            }

            Path sourceDirectory = Path.of(snapshot.sourceDirectory())
                    .toAbsolutePath().normalize();
            Path normalizedProjectsRoot = projectsRoot.toAbsolutePath().normalize();
            Path normalizedProjectDirectory = projectsRoot.resolve(projectName)
                    .toAbsolutePath().normalize();

            if (!sourceDirectory.startsWith(normalizedProjectDirectory)
                    || !normalizedProjectDirectory.startsWith(normalizedProjectsRoot)
                    || !Files.isDirectory(sourceDirectory)) {
                throw new IOException("مسیر سورس در فایل نمایه معتبر نیست.");
            }

            return new ProjectIndex(
                    snapshot.projectName(),
                    snapshot.classes(),
                    snapshot.calls(),
                    snapshot.errors(),
                    sourceDirectory,
                    snapshot.sourceFileCount(),
                    snapshot.parsedFileCount()
            );
        } catch (IOException | RuntimeException exception) {
            // اگر JSON خراب باشد، restoreLastProject نمایه را از سورس بازسازی می‌کند.
            System.err.println("بارگذاری index.json ناموفق بود: " + exception.getMessage());
            return null;
        }
    }

    private ProjectIndex createIndex(
            String projectName,
            Path sourceDirectory) throws IOException {

        Map<String, ClassInfo> classes = new LinkedHashMap<>();
        List<MethodCallInfo> calls = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int sourceFileCount = 0;
        int parsedFileCount = 0;

        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            List<Path> javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString()
                            .toLowerCase(Locale.ROOT).endsWith(".java"))
                    .sorted()
                    .toList();

            sourceFileCount = javaFiles.size();

            for (Path javaFile : javaFiles) {
                String path = sourceDirectory.relativize(javaFile)
                        .toString().replace('\\', '/');

                try {
                    long size = Files.size(javaFile);

                    if (size > MAX_FILE_BYTES) {
                        errors.add(path + ": فایل از حد مجاز بزرگ‌تر است.");
                        continue;
                    }

                    String source = Files.readString(
                            javaFile,
                            StandardCharsets.UTF_8
                    );

                    ParseResult<CompilationUnit> parsed =
                            parser.parse(source);

                    if (parsed.getResult().isEmpty()
                            || !parsed.getProblems().isEmpty()) {
                        errors.add(path + ": خطای parse");
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

                    for (TypeDeclaration<?> type : unit.getTypes()) {
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
                    errors.add(path + ": خطای تحلیل ساختاری");
                }
            }
        }

        return new ProjectIndex(
                projectName,
                classes,
                calls,
                errors,
                sourceDirectory,
                sourceFileCount,
                parsedFileCount
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

    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(
                    java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
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

    public record ProjectIndexSnapshot(
            String projectName,
            Map<String, ClassInfo> classes,
            List<MethodCallInfo> calls,
            List<String> errors,
            String sourceDirectory,
            int sourceFileCount,
            int parsedFileCount
    ) {}

    public record ProjectIndex(
            String projectName,
            Map<String, ClassInfo> classes,
            List<MethodCallInfo> calls,
            List<String> errors,
            Path sourceDirectory,
            int sourceFileCount,
            int parsedFileCount
    ) {}
}
