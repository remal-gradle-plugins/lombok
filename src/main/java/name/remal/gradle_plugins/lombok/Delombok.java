package name.remal.gradle_plugins.lombok;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.deleteRecursively;
import static org.gradle.api.tasks.PathSensitivity.ABSOLUTE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.ObjectUtils;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.StopActionException;
import org.gradle.process.JavaExecSpec;

@CacheableTask
public abstract class Delombok extends AbstractLombokTask {

    public Delombok() {
        super("delombok");

        val project = getProject();

        getOutputDir().convention(
            project.getLayout().getBuildDirectory().dir(getName())
        );
    }

    @Getter
    @Nested
    private final DelombokFormat format = getObjectFactory().newInstance(DelombokFormat.class);

    public void format(Action<DelombokFormat> action) {
        action.execute(format);
    }

    @org.gradle.api.tasks.Optional
    @Input
    public abstract Property<String> getEncoding();

    @org.gradle.api.tasks.Optional
    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getClasspath();


    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();


    @SkipWhenEmpty
    @org.gradle.api.tasks.Optional
    @InputFiles
    @IgnoreEmptyDirectories
    @PathSensitive(ABSOLUTE)
    public abstract ConfigurableFileCollection getInputFiles();


    @Override
    @SneakyThrows
    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    protected List<String> createArgs() {
        List<String> args = new ArrayList<>();

        args.addAll(getFormat().toArgs());

        Optional.ofNullable(getEncoding().getOrNull())
            .filter(ObjectUtils::isNotEmpty)
            .ifPresent(value -> args.add(String.format("--encoding=%s", value)));

        args.add(String.format("--classpath=%s", getClasspath().getFiles().stream()
            .filter(File::exists)
            .map(File::getAbsolutePath)
            .collect(joining(File.pathSeparator))
        ));

        getOutputDir().finalizeValue();
        args.add(String.format("--target=%s", getOutputDir().get().getAsFile().getAbsolutePath()));

        val inputFiles = getInputFiles().getFiles().stream()
            .filter(File::exists)
            .collect(toList());
        if (inputFiles.isEmpty()) {
            throw new StopActionException();
        }
        inputFiles.stream()
            .map(File::getAbsolutePath)
            .forEach(args::add);


        val argsFileContent = args.stream()
            .map(arg -> arg.replace("\\", "\\\\"))
            .map(arg -> arg.replace(" ", "\\ "))
            .collect(joining("\n"));
        val argsFile = new File(getTemporaryDir(), getName() + ".args");
        write(createParentDirectories(argsFile.toPath()), argsFileContent.getBytes(UTF_8));

        return singletonList('@' + argsFile.getAbsolutePath());
    }

    @Override
    @SneakyThrows
    protected void beforeExecute(JavaExecSpec execSpec) {
        val outputDir = getOutputDir().get().getAsFile();
        deleteRecursively(outputDir.toPath());
        createDirectories(outputDir.toPath());
    }

}
