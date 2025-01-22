package name.remal.gradle_plugins.lombok.config;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.FileUtils;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

@NoArgsConstructor(access = PRIVATE)
public abstract class LombokConfigUtils {

    public static List<LombokConfig> parseLombokConfigs(Collection<File> sourceDirs) {
        sourceDirs = sourceDirs.stream()
            .distinct()
            .map(FileUtils::normalizeFile)
            .distinct()
            .sorted()
            .collect(toList());

        Map<List<LombokConfigPath>, LombokConfig> lombokConfigMap = new LinkedHashMap<>();
        for (var sourceDir : sourceDirs) {
            var lombokConfig = new LombokConfig(sourceDir);
            var lombokConfigFiles = lombokConfig.getConfigFiles().stream()
                .map(LombokConfigFile::getFile)
                .collect(toList());
            lombokConfigMap.computeIfAbsent(lombokConfigFiles, __ -> lombokConfig);
        }

        return new ArrayList<>(lombokConfigMap.values());
    }

    public static List<LombokConfig> parseLombokConfigs(JavaCompile task) {
        var extensions = task.getExtensions();
        LombokConfigs container = extensions.findByType(LombokConfigs.class);
        if (container == null) {
            var dirs = Stream.concat(
                streamJavaCompileSourceDirs(task),
                Stream.of(task.getProject().getProjectDir())
            ).collect(toList());
            var configs = ImmutableList.copyOf(parseLombokConfigs(dirs));
            container = new LombokConfigs(configs);
            extensions.add("lombok-configs-container", container);
        }
        return container.getConfigs();
    }

    @RequiredArgsConstructor
    @Getter
    private static class LombokConfigs {
        private final List<LombokConfig> configs;
    }


    @SuppressWarnings("ConstantConditions")
    public static Stream<File> streamJavaCompileSourceDirs(JavaCompile task) {
        var sourceDirStream = Stream.of(task.getSource()).filter(Objects::nonNull)
            .map(FileTree::getFiles).filter(Objects::nonNull)
            .flatMap(Collection::stream).filter(Objects::nonNull)
            .map(File::getAbsoluteFile)
            .map(File::getParentFile);

        var generatedSourceDirStream = Stream.of(task.getOptions()).filter(Objects::nonNull)
            .map(CompileOptions::getGeneratedSourceOutputDirectory).filter(Objects::nonNull)
            .map(DirectoryProperty::getAsFile).filter(Objects::nonNull)
            .map(Provider::getOrNull).filter(Objects::nonNull);

        return Stream.concat(sourceDirStream, generatedSourceDirStream)
            .distinct();
    }

}
