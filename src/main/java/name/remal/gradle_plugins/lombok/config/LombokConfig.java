package name.remal.gradle_plugins.lombok.config;

import static java.nio.file.Files.isRegularFile;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverse;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toUnmodifiableList;
import static name.remal.gradle_plugins.lombok.config.LombokConfigFileParser.parseLombokConfigFile;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.doNotInline;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.ToString;
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
        return getConfigFiles().stream()
            .map(LombokConfigFile::getFile)
            .map(LombokConfigPath::getFileSystemPath)
            .collect(toUnmodifiableList());
    }


    @Override
    public Stream<LombokConfigFileProperty> streamProperties() {
        return getConfigFiles().stream()
            .flatMap(LombokConfigFile::streamProperties);
    }


    @Getter(lazy = true)
    private final List<LombokConfigFile> configFiles = resolveConfigFiles();

    private List<LombokConfigFile> resolveConfigFiles() {
        var result = new ArrayList<LombokConfigFile>();
        var isStopped = new AtomicBoolean(false);
        for (var currentDir = dir; currentDir != null; currentDir = currentDir.getParent()) {
            var file = currentDir.resolve(LOMBOK_CONFIG_FILE_NAME);
            if (isRegularFile(file)) {
                processFile(isStopped, result, new LombokConfigPathSystem(file), emptyList());
                if (isStopped.get()) {
                    break;
                }
            }
        }
        reverse(result);
        return List.copyOf(result);
    }

    private static void processFile(
        AtomicBoolean isStopped,
        List<LombokConfigFile> result,
        LombokConfigPath file,
        @Unmodifiable List<ImportTraceElement> importTrace
    ) {
        var configFile = parseLombokConfigFile(file);

        if (!importTrace.isEmpty()) {
            configFile = configFile.withImportTrace(importTrace);
        }

        result.add(configFile);

        if (configFile.isStopBubbling()) {
            isStopped.set(true);
        }


        var resolvedImports = configFile.getResolvedImports().stream()
            .filter(ResolvedImportFile.class::isInstance)
            .map(ResolvedImportFile.class::cast)
            .collect(toCollection(ArrayList::new));
        reverse(resolvedImports);
        for (var resolvedImport : resolvedImports) {
            var newImportTrace = new ArrayList<>(importTrace);
            newImportTrace.add(ImportTraceElement.builderFor(resolvedImport).build());

            processFile(isStopped, result, resolvedImport.getFileToImport(), List.copyOf(newImportTrace));
        }
    }

}
