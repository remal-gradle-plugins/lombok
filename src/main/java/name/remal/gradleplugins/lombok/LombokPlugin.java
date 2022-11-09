package name.remal.gradleplugins.lombok;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static name.remal.gradleplugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorFilesOrder;
import static name.remal.gradleplugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorsOrder;
import static name.remal.gradleplugins.lombok.JavacPackagesToOpenUtils.shouldJavacPackageOpenJvmArgsBeAdded;
import static name.remal.gradleplugins.lombok.JavacPackagesToOpenUtils.withJavacPackageOpens;
import static name.remal.gradleplugins.lombok.LombokDependencies.getLombokDependency;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradleplugins.toolkit.ObjectUtils.defaultTrue;
import static name.remal.gradleplugins.toolkit.ObjectUtils.doNotInline;
import static name.remal.gradleplugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradleplugins.toolkit.TaskUtils.doBeforeTaskExecution;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.CustomLog;
import lombok.val;
import name.remal.gradleplugins.toolkit.JavaInstallationMetadataUtils;
import name.remal.gradleplugins.toolkit.ObjectUtils;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.toolchain.JavaCompiler;

@CustomLog
public class LombokPlugin implements Plugin<Project> {

    public static final String LOMBOK_EXTENSION_NAME = doNotInline("lombok");
    public static final String LOMBOK_CONFIGURATION_NAME = doNotInline("lombok");
    public static final String DELOMBOK_TASK_NAME = doNotInline("delombok");

    private Project project;
    private LombokExtension lombokExtension;
    private Configuration lombokConf;

    @Override
    public void apply(Project project) {
        this.project = project;

        this.lombokExtension = project.getExtensions().create(LOMBOK_EXTENSION_NAME, LombokExtension.class);

        this.lombokConf = project.getConfigurations().create(LOMBOK_CONFIGURATION_NAME, conf -> {
            conf.setDescription("Lombok");
            conf.defaultDependencies(deps -> {
                deps.add(createDependency(
                    project,
                    getLombokDependency("lombok"),
                    lombokExtension.getLombokVersion().get()
                ));
            });
        });

        configureLombokTasks();
        configureJavacReflectionsAccess();
        configureAnnotationProcessorsOrder();

        project.getPluginManager().withPlugin("java", __ -> {
            configureSourceSetConfigurations();
            configureDelombokForAllSourceSets();
        });
    }


    private void configureLombokTasks() {
        project.getTasks().withType(AbstractLombokTask.class, task -> {
            task.getToolClasspath().setFrom(lombokConf);
        });
    }


    @SuppressWarnings("java:S3776")
    private void configureJavacReflectionsAccess() {
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            doBeforeTaskExecution(task, __ -> {
                val compileOptions = task.getOptions();
                if (compileOptions == null) {
                    return;
                }

                if (!defaultTrue(lombokExtension.getOpenJavacPackages().getOrNull())) {
                    return;
                }

                val sourceCompatibility = Optional.ofNullable(task.getSourceCompatibility())
                    .map(JavaVersion::toVersion)
                    .orElse(null);
                if (sourceCompatibility != null && !shouldJavacPackageOpenJvmArgsBeAdded(sourceCompatibility)) {
                    return;
                }

                val targetCompatibility = Optional.ofNullable(task.getTargetCompatibility())
                    .map(JavaVersion::toVersion)
                    .orElse(null);
                if (targetCompatibility != null && !shouldJavacPackageOpenJvmArgsBeAdded(targetCompatibility)) {
                    return;
                }

                val compilerJavaVersion = Optional.ofNullable(task.getJavaCompiler())
                    .map(Provider::getOrNull)
                    .map(JavaCompiler::getMetadata)
                    .map(JavaInstallationMetadataUtils::getJavaInstallationVersionOf)
                    .orElseGet(JavaVersion::current);
                if (!shouldJavacPackageOpenJvmArgsBeAdded(compilerJavaVersion)) {
                    return;
                }

                List<String> compilerArgs = compileOptions.getCompilerArgs();
                compilerArgs = withJavacPackageOpens(compilerArgs);
                compileOptions.setCompilerArgs(compilerArgs);
            });
        });
    }


    @SuppressWarnings({"UnstableApiUsage", "java:S3776"})
    private void configureAnnotationProcessorsOrder() {
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            doBeforeTaskExecution(task, __ -> {
                val compileOptions = task.getOptions();
                if (compileOptions == null) {
                    return;
                }

                if (!defaultTrue(lombokExtension.getFixAnnotationProcessorsOrder().getOrNull())) {
                    return;
                }

                val annotationProcessorPath = compileOptions.getAnnotationProcessorPath();
                if (annotationProcessorPath == null) {
                    return;
                }

                compileOptions.setAnnotationProcessorPath(project.files(project.provider(() -> {
                    Collection<File> files = annotationProcessorPath.getFiles();
                    files = withFixedAnnotationProcessorFilesOrder(files);
                    return files;
                })));
            });

            doBeforeTaskExecution(task, __ -> {
                val compileOptions = task.getOptions();
                if (compileOptions == null) {
                    return;
                }

                if (!defaultTrue(lombokExtension.getFixAnnotationProcessorsOrder().getOrNull())) {
                    return;
                }

                List<String> compilerArgs = compileOptions.getCompilerArgs();
                if (isEmpty(compilerArgs)) {
                    return;
                }

                int processorIndex = compilerArgs.lastIndexOf("-processor");
                if (processorIndex < 0 && processorIndex >= compilerArgs.size() - 1) {
                    return;
                }

                String processorsArg = compilerArgs.get(processorIndex + 1);
                val processors = Splitter.on(',').splitToStream(processorsArg)
                    .map(String::trim)
                    .filter(ObjectUtils::isNotEmpty)
                    .collect(toList());
                if (processors.isEmpty()) {
                    return;
                }

                val fixedProcessors = withFixedAnnotationProcessorsOrder(processors);
                processorsArg = Joiner.on(',').join(fixedProcessors);

                compilerArgs = new ArrayList<>(compilerArgs);
                compilerArgs.set(processorIndex + 1, processorsArg);
                compileOptions.setCompilerArgs(compilerArgs);
            });
        });
    }


    private void configureSourceSetConfigurations() {
        getExtension(project, SourceSetContainer.class).all(sourceSet -> {
            configureConfiguration(project, sourceSet.getCompileOnlyConfigurationName(), conf -> {
                conf.extendsFrom(lombokConf);
            });

            configureConfiguration(project, sourceSet.getAnnotationProcessorConfigurationName(), conf -> {
                conf.extendsFrom(lombokConf);

                conf.getDependencies().add(createDependency(
                    project,
                    getLombokDependency("lombok-mapstruct-binding")
                ));
            });
        });
    }


    private void configureDelombokForAllSourceSets() {
        getExtension(project, SourceSetContainer.class).all(sourceSet -> {
            val delombokTaskName = getDelombokTaskNameFor(sourceSet);
            val delombokProvider = project.getTasks().register(delombokTaskName, Delombok.class, delombok -> {
                delombok.dependsOn(sourceSet.getClassesTaskName());

                val javaCompileProvider = project.getTasks().named(
                    sourceSet.getCompileJavaTaskName(),
                    JavaCompile.class
                );

                delombok.getEncoding().set(project.provider(() ->
                    javaCompileProvider.get().getOptions().getEncoding()
                ));

                delombok.getClasspath().setFrom(project.provider(() ->
                    javaCompileProvider.get().getClasspath()
                ));

                delombok.getInputFiles().setFrom(sourceSet.getAllJava().getSourceDirectories());
                delombok.getInputFiles().from(project.provider(() ->
                    javaCompileProvider.get().getOptions().getGeneratedSourceOutputDirectory()
                ));
            });

            val javadocTaskName = sourceSet.getJavadocTaskName();
            project.getTasks().withType(Javadoc.class)
                .matching(it -> it.getName().equals(javadocTaskName))
                .configureEach(javadoc -> {
                    javadoc.dependsOn(delombokProvider);
                    javadoc.setSource(delombokProvider.map(Delombok::getOutputDir));
                });
        });
    }


    private static String getDelombokTaskNameFor(SourceSet sourceSet) {
        return sourceSet.getTaskName(DELOMBOK_TASK_NAME, "");
    }

    private static void configureConfiguration(Project project, String name, Action<Configuration> action) {
        project.getConfigurations().matching(it -> Objects.equals(it.getName(), name)).all(action);
    }

    private static Dependency createDependency(Project project, LombokDependency lombokDependency) {
        return project.getDependencies().create(format(
            "%s:%s:%s",
            lombokDependency.getGroup(),
            lombokDependency.getName(),
            lombokDependency.getVersion()
        ));
    }

    private static Dependency createDependency(Project project, LombokDependency lombokDependency, String version) {
        return project.getDependencies().create(format(
            "%s:%s:%s",
            lombokDependency.getGroup(),
            lombokDependency.getName(),
            version
        ));
    }

}
