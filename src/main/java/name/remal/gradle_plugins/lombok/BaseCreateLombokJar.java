package name.remal.gradle_plugins.lombok;

import static java.lang.String.format;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;

import com.google.errorprone.annotations.ForOverride;
import java.io.File;
import lombok.SneakyThrows;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.process.JavaExecSpec;

abstract class BaseCreateLombokJar extends AbstractLombokTask {

    @ForOverride
    @Internal
    protected abstract String getGeneratedFileName();


    protected BaseCreateLombokJar(String command) {
        super(command);

        getOutputFile().convention(
            getProjectLayout().getBuildDirectory().file(getName() + '/' + getGeneratedFileName())
        );
    }


    @OutputFile
    public abstract RegularFileProperty getOutputFile();


    @Override
    @SneakyThrows
    protected void afterExecute(JavaExecSpec execSpec) {
        var generatedFileName = getGeneratedFileName();
        var generatedFile = new File(execSpec.getWorkingDir(), generatedFileName);
        if (!generatedFile.isFile()) {
            throw new GradleException(format(
                "%s file can't be found: %s",
                generatedFileName,
                generatedFile.getAbsolutePath()
            ));
        }

        var outputFile = getOutputFile().get().getAsFile();

        move(
            generatedFile.toPath(),
            createParentDirectories(outputFile.toPath()),
            REPLACE_EXISTING
        );
    }

}
