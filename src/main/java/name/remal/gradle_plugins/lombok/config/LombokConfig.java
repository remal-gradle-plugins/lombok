package name.remal.gradle_plugins.lombok.config;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.lombok.config.LombokConfigFileParser.parseLombokConfigFile;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.doNotInline;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;

@ToString(of = "dir")
public class LombokConfig implements WithProperties {

    public static final String LOMBOK_CONFIG_FILE_NAME = doNotInline("lombok.config");


    @Getter
    private final Path dir;

    public LombokConfig(Path path) {
        path = normalizePath(path);

        if (isRegularFile(path)) {
            path = requireNonNull(path.getParent(), "parent");
        }

        this.dir = path;
    }

    public LombokConfig(File file) {
        this(file.toPath());
    }


    public Path getPath() {
        return getConfigFiles().stream()
            .map(LombokConfigFile::getFile)
            .filter(LombokConfigPathSystem.class::isInstance)
            .map(LombokConfigPathSystem.class::cast)
            .map(LombokConfigPathSystem::getPath)
            .reduce((first, second) -> second)
            .orElseGet(this::getDir);
    }


    @Unmodifiable
    public List<Path> getInvolvedPaths() {
        return unmodifiableList(
            getConfigFiles().stream()
                .map(LombokConfigFile::getFile)
                .map(LombokConfigPath::getFileSystemPath)
                .collect(toList())
        );
    }


    @Override
    @Unmodifiable
    public List<LombokConfigFileProperty> getProperties() {
        return unmodifiableList(
            getConfigFiles().stream()
                .map(LombokConfigFile::getProperties)
                .flatMap(Collection::stream)
                .collect(toList())
        );
    }


    @Getter(lazy = true)
    private final List<LombokConfigFile> configFiles = resolveConfigFiles();

    private List<LombokConfigFile> resolveConfigFiles() {
        List<LombokConfigFile> result = new ArrayList<>();
        val isStopped = new AtomicBoolean(false);
        for (Path currentDir = dir; currentDir != null; currentDir = currentDir.getParent()) {
            val file = currentDir.resolve(LOMBOK_CONFIG_FILE_NAME);
            if (exists(file)) {
                processFile(isStopped, result, new LombokConfigPathSystem(file), emptyList());
                if (isStopped.get()) {
                    break;
                }
            }
        }
        reverse(result);
        return unmodifiableList(result);
    }

    private static void processFile(
        AtomicBoolean isStopped,
        List<LombokConfigFile> result,
        LombokConfigPath file,
        List<ImportTraceElement> importTrace
    ) {
        LombokConfigFile configFile = parseLombokConfigFile(file);

        if (!importTrace.isEmpty()) {
            configFile = configFile.withImportTrace(importTrace);
        }

        result.add(configFile);

        if (configFile.isStopBubbling()) {
            isStopped.set(true);
        }


        List<ResolvedImportFile> resolvedImports = configFile.getResolvedImports().stream()
            .filter(ResolvedImportFile.class::isInstance)
            .map(ResolvedImportFile.class::cast)
            .collect(toCollection(ArrayList::new));
        reverse(resolvedImports);
        for (val resolvedImport : resolvedImports) {
            val currentImportTrace = new ArrayList<>(importTrace);
            currentImportTrace.add(ImportTraceElement.builderFor(resolvedImport).build());

            processFile(isStopped, result, resolvedImport.getFileToImport(), unmodifiableList(currentImportTrace));
        }
    }

}
