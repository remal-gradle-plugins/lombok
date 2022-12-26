package name.remal.gradle_plugins.lombok.config;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static name.remal.gradle_plugins.toolkit.LayoutUtils.getRootDirOf;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import lombok.val;
import name.remal.gradle_plugins.lombok.config.LombokExtensionConfigGenerate.ConfigProperty;
import name.remal.gradle_plugins.toolkit.EditorConfig;
import name.remal.gradle_plugins.toolkit.PathIsOutOfRootPathException;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

@CacheableTask
public abstract class GenerateLombokConfig extends DefaultTask {

    private static final String PLUGIN_ID = "name.remal.lombok";
    private static final String GENERATED_MARKER = "# This file is generated by `" + PLUGIN_ID + "` plugin.";

    private static final List<String> GENERATED_HEADER_LINES = asList(
        GENERATED_MARKER,
        ""
    );


    @OutputFile
    public abstract RegularFileProperty getFile();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract ListProperty<ConfigProperty> getProperties();

    @TaskAction
    public void execute() throws Throwable {
        val targetPath = normalizePath(getFile().getAsFile().get().toPath());
        createParentDirectories(targetPath);

        if (exists(targetPath)) {
            val content = new String(readAllBytes(targetPath), UTF_8);
            if (!content.trim().startsWith(GENERATED_MARKER)) {
                getLogger().warn("{} doesn't start with '{}'", targetPath, GENERATED_MARKER);
                throw new GradleException(format(
                    "%S: Lombok config file was generated not by `%s` Gradle plugin."
                        + " Consider removing it to allow the Gradle plugin to regenerate it."
                        + " Another option is to disable the generating functionality.",
                    targetPath,
                    PLUGIN_ID
                ));
            }
        }

        String lineSeparator;
        try {
            val editorConfig = new EditorConfig(getRootDir().getAsFile().get().toPath());
            lineSeparator = editorConfig.getLineSeparatorFor(targetPath);
        } catch (PathIsOutOfRootPathException ignored) {
            lineSeparator = "\n";
        }

        try (val writer = newBufferedWriter(targetPath, UTF_8)) {
            for (val line : GENERATED_HEADER_LINES) {
                writer.append(line).append(lineSeparator);
            }

            val propertyMap = getProperties().get().stream()
                .collect(groupingBy(
                    ConfigProperty::getKey,
                    LinkedHashMap::new,
                    toCollection(ArrayList::new)
                ));
            for (val propertyEntry : propertyMap.entrySet()) {
                val key = propertyEntry.getKey();
                List<ConfigProperty> values = propertyEntry.getValue();

                val authoritativeIndex = lastIndexOf(values, it -> it.getOperator().isAuthoritative());
                if (authoritativeIndex >= 0) {
                    values = values.subList(authoritativeIndex, values.size());
                }

                for (val value : values) {
                    switch (value.getOperator()) {
                        case SET:
                            writer.append(key).append(" = ").append(value.getValue());
                            break;
                        case PLUS:
                            writer.append(key).append(" += ").append(value.getValue());
                            break;
                        case MINUS:
                            writer.append(key).append(" -= ").append(value.getValue());
                            break;
                        case CLEAR:
                            writer.append("clear ").append(key);
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsupported operator: " + value.getOperator());
                    }
                    writer.append(lineSeparator);
                }
            }
        }

        setDidWork(true);
    }


    @Internal
    protected abstract DirectoryProperty getRootDir();

    {
        val project = getProject();
        getRootDir().set(project.getLayout().dir(project.provider(() ->
            getRootDirOf(project)
        )));
    }


    private static <T> int lastIndexOf(List<T> list, Predicate<T> condition) {
        for (int i = list.size() - 1; 0 <= i; --i) {
            val item = list.get(i);
            if (condition.test(item)) {
                return i;
            }
        }
        return -1;
    }

}