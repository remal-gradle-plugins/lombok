package name.remal.gradleplugins.lombok;

import static java.util.Collections.emptyList;
import static name.remal.gradleplugins.lombok.JavacPackagesToOpenUtils.getJavacPackageOpenJvmArgs;
import static name.remal.gradleplugins.lombok.JavacPackagesToOpenUtils.shouldJavacPackageOpenJvmArgsBeAdded;
import static name.remal.gradleplugins.toolkit.JavaLauncherUtils.getJavaLauncherProviderFor;

import com.google.errorprone.annotations.ForOverride;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.val;
import name.remal.gradleplugins.toolkit.JavaInstallationMetadataUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.JavaVersion;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.JavaExecSpec;

public abstract class AbstractLombokTask extends DefaultTask {

    private static final String MAIN_CLASS = "lombok.launch.Main";


    @ForOverride
    @Internal
    protected List<String> createArgs() {
        return emptyList();
    }

    @ForOverride
    protected void beforeExecute(JavaExecSpec execSpec) {
    }

    @ForOverride
    protected void afterExecute(JavaExecSpec execSpec) {
    }


    @Classpath
    @InputFiles
    public abstract ConfigurableFileCollection getToolClasspath();

    @org.gradle.api.tasks.Optional
    @Nested
    public abstract Property<JavaLauncher> getJavaLauncher();


    private final String command;

    protected AbstractLombokTask(String command) {
        this.command = command;
        getJavaLauncher().convention(getJavaLauncherProviderFor(getProject()));
    }


    @TaskAction
    public void execute() {
        AtomicReference<JavaExecSpec> execSpecRef = new AtomicReference<>();

        getProject().javaexec(execSpec -> {
            val javaLauncher = getJavaLauncher().get();
            val javaVersion = Optional.of(javaLauncher.getMetadata())
                .map(JavaInstallationMetadataUtils::getJavaInstallationVersionOf)
                .orElseGet(JavaVersion::current);
            if (shouldJavacPackageOpenJvmArgsBeAdded(javaVersion)) {
                execSpec.jvmArgs(getJavacPackageOpenJvmArgs());
            }
            execSpec.setExecutable(javaLauncher.getExecutablePath().getAsFile());
            execSpec.setClasspath(getToolClasspath());
            execSpec.getMainClass().set(MAIN_CLASS);
            execSpec.args(command);
            execSpec.args(createArgs());
            execSpec.setWorkingDir(getTemporaryDir());
            beforeExecute(execSpec);
            execSpecRef.set(execSpec);
        });

        afterExecute(execSpecRef.get());
    }

}
