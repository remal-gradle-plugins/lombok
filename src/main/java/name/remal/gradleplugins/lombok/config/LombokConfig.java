package name.remal.gradleplugins.lombok.config;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.lombok.config.LombokConfigFileParser.parseLombokConfigFile;
import static name.remal.gradleplugins.lombok.config.LombokConfigFileProperty.byLombokConfigKey;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.CLEAR;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.MINUS;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.PLUS;
import static name.remal.gradleplugins.toolkit.ObjectUtils.doNotInline;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

@ToString(of = "dir")
public class LombokConfig {

    public static final String LOMBOK_CONFIG_FILE_NAME = doNotInline("lombok.config");


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


    @UnmodifiableView
    public List<Path> getInvolvedPaths() {
        return unmodifiableList(
            getAllConfigFiles().stream()
                .map(LombokConfigFile::getFile)
                .map(LombokConfigPath::getFileSystemPath)
                .collect(toList())
        );
    }

    @Unmodifiable
    public List<LombokConfigFileProperty> getAllProperties() {
        return unmodifiableList(
            getAllConfigFiles().stream()
                .map(LombokConfigFile::getProperties)
                .flatMap(Collection::stream)
                .collect(toList())
        );
    }

    @Nullable
    public String get(String key) {
        val property = getAllConfigFiles().stream()
            .map(LombokConfigFile::getProperties)
            .flatMap(Collection::stream)
            .filter(byLombokConfigKey(key))
            .reduce((first, second) -> second)
            .orElse(null);

        if (property == null || property.getOperator() == CLEAR) {
            return null;
        }

        return property.getValue();
    }

    @Unmodifiable
    public List<String> getList(String key) {
        Set<String> result = new LinkedHashSet<>();

        for (val configFile : getAllConfigFiles()) {
            for (val property : configFile.getProperties()) {
                if (!property.is(key)) {
                    continue;
                }

                val operator = property.getOperator();
                if (operator == PLUS) {
                    result.add(property.getValue());
                } else if (operator == MINUS) {
                    result.remove(property.getValue());
                } else if (operator == CLEAR) {
                    result.clear();
                }
            }
        }

        return ImmutableList.copyOf(result);
    }


    @Getter(value = PRIVATE, lazy = true)
    private final List<LombokConfigFile> allConfigFiles = resolveAllConfigFiles();

    private List<LombokConfigFile> resolveAllConfigFiles() {
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
