package name.remal.gradleplugins.lombok;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static name.remal.gradleplugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorFilesOrder;
import static name.remal.gradleplugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorsOrder;
import static name.remal.gradleplugins.lombok.JavacPackagesToOpenUtils.shouldJavacPackageOpenJvmArgsBeAdded;
import static name.remal.gradleplugins.lombok.JavacPackagesToOpenUtils.withJavacPackageOpens;
import static name.remal.gradleplugins.lombok.LombokDependencies.getLombokDependency;
import static name.remal.gradleplugins.lombok.config.LombokConfigUtils.parseLombokConfigs;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradleplugins.toolkit.ObjectUtils.defaultTrue;
import static name.remal.gradleplugins.toolkit.ObjectUtils.doNotInline;
import static name.remal.gradleplugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradleplugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradleplugins.toolkit.ProjectUtils.afterEvaluateOrNow;
import static name.remal.gradleplugins.toolkit.TaskUtils.doBeforeTaskExecution;
import static org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE;
import static org.gradle.api.attributes.Category.LIBRARY;
import static org.gradle.api.attributes.Usage.JAVA_RUNTIME;
import static org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE;
import static org.gradle.api.plugins.JavaBasePlugin.CHECK_TASK_NAME;
import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.CustomLog;
import lombok.val;
import name.remal.gradleplugins.lombok.config.GenerateLombokConfig;
import name.remal.gradleplugins.lombok.config.LombokConfig;
import name.remal.gradleplugins.lombok.config.LombokConfigUtils;
import name.remal.gradleplugins.lombok.config.ValidateLombokConfig;
import name.remal.gradleplugins.toolkit.JavaInstallationMetadataUtils;
import name.remal.gradleplugins.toolkit.ObjectUtils;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.toolchain.JavaCompiler;

@CustomLog
public abstract class LombokPlugin implements Plugin<Project> {

    public static final String LOMBOK_EXTENSION_NAME = doNotInline("lombok");
    public static final String LOMBOK_CONFIGURATION_NAME = doNotInline("lombok");
    public static final String DELOMBOK_TASK_NAME = doNotInline("delombok");
    public static final String VALIDATE_LOMBOK_CONFIG_TASK_NAME = doNotInline("validateLombokConfig");
    public static final String GENERATE_LOMBOK_CONFIG_TASK_NAME = doNotInline("generateLombokConfig");

    private Project project;
    private LombokExtension lombokExtension;
    private Configuration lombokConf;

    @Override
    public void apply(Project project) {
        this.project = project;

        this.lombokExtension = project.getExtensions().create(LOMBOK_EXTENSION_NAME, LombokExtension.class);

        val lombokDependency = getLombokDependency("lombok");
        this.lombokConf = project.getConfigurations().create(LOMBOK_CONFIGURATION_NAME, conf -> {
            conf.setDescription("Lombok");
            conf.defaultDependencies(deps -> {
                deps.add(createDependency(
                    project,
                    lombokDependency,
                    lombokExtension.getLombokVersion().getOrNull()
                ));
            });

            conf.setCanBeConsumed(false);
            conf.attributes(attrs -> {
                attrs.attribute(
                    USAGE_ATTRIBUTE,
                    project.getObjects().named(Usage.class, JAVA_RUNTIME)
                );
                attrs.attribute(
                    CATEGORY_ATTRIBUTE,
                    project.getObjects().named(Category.class, LIBRARY)
                );
            });
        });

        lombokExtension.getLombokVersion().convention(project.provider(() ->
            lombokConf.getAllDependencyConstraints().stream()
                .filter(constraint -> lombokDependency.getGroup().equals(constraint.getGroup()))
                .filter(constraint -> lombokDependency.getName().equals(constraint.getName()))
                .map(DependencyConstraint::getVersion)
                .filter(ObjectUtils::isNotEmpty)
                .reduce((first, second) -> second)
                .orElseGet(lombokDependency::getVersion)
        ));


        configureLombokTasks();
        configureJavacReflectionsAccess();
        configureAnnotationProcessorsOrder();
        configureCompileInputFiles();
        configureConfigValidation();
        configureConfigGeneration();

        project.getPluginManager().withPlugin("java", __ -> {
            configureSourceSetConfigurations();
            configureDelombokForAllSourceSets();
        });
    }


    private void configureLombokTasks() {
        project.getTasks().withType(AbstractLombokTask.class, task -> {
            task.getToolClasspath().setFrom(lombokConf);
        });

        project.getTasks().withType(Delombok.class, task -> {
            task.getFormat().convention(lombokExtension.getDelombok().getFormat());
        });
    }


    @SuppressWarnings("java:S3776")
    private void configureJavacReflectionsAccess() {
        val isEnabled = lombokExtension.getFixJavacReflectionsAccess();
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            doBeforeTaskExecution(task, __ -> {
                val compileOptions = task.getOptions();
                if (compileOptions == null) {
                    return;
                }

                if (!defaultTrue(isEnabled.getOrNull())) {
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
        val isEnabled = lombokExtension.getFixAnnotationProcessorsOrder();
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            doBeforeTaskExecution(task, __ -> {
                val compileOptions = task.getOptions();
                if (compileOptions == null) {
                    return;
                }

                if (!defaultTrue(isEnabled.getOrNull())) {
                    return;
                }

                val annotationProcessorPath = compileOptions.getAnnotationProcessorPath();
                if (annotationProcessorPath == null) {
                    return;
                }

                val project = task.getProject();
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

                if (!defaultTrue(isEnabled.getOrNull())) {
                    return;
                }

                List<String> compilerArgs = compileOptions.getCompilerArgs();
                if (isEmpty(compilerArgs)) {
                    return;
                }

                int processorIndex = compilerArgs.lastIndexOf("-processor");
                if (processorIndex < 0 || processorIndex >= compilerArgs.size() - 1) {
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


    private void configureCompileInputFiles() {
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            task.getInputs().files(task.getProject().provider(() ->
                parseLombokConfigs(task).stream()
                    .map(LombokConfig::getInvolvedPaths)
                    .flatMap(Collection::stream)
                    .collect(toSet())
            )).optional(true).withPathSensitivity(RELATIVE);
        });
    }


    private void configureConfigValidation() {
        val tasks = project.getTasks();
        val extensionDisabledRules = lombokExtension.getConfig().getValidate().getDisabledRules();
        project.getTasks().register(VALIDATE_LOMBOK_CONFIG_TASK_NAME, ValidateLombokConfig.class, task -> {
            val javaCompileTasks = tasks.withType(JavaCompile.class);
            task.dependsOn(javaCompileTasks);

            task.getDirectories().from(getProjectLayout().getProjectDirectory());
            task.getDirectories().from(getProviders().provider(() ->
                javaCompileTasks.stream()
                    .flatMap(LombokConfigUtils::streamJavaCompileSourceDirs)
                    .collect(toSet())
            ));

            task.getDisabledRules().convention(extensionDisabledRules);
        });

        project.getPluginManager().withPlugin("java", __ -> {
            project.getTasks().named(CHECK_TASK_NAME, task -> {
                task.dependsOn(tasks.withType(ValidateLombokConfig.class));
            });
        });
    }


    private void configureConfigGeneration() {
        afterEvaluateOrNow(project, __ -> {
            val isEnabled = lombokExtension.getConfig().getGenerate().getEnabled().getOrNull();
            if (FALSE.equals(isEnabled)) {
                return; // disabled explicitly
            }

            if (isEnabled == null) {
                if (isNotEmpty(lombokExtension.getConfig().getGenerate().getProperties().getOrNull())) {
                    logger.warn("`" + LOMBOK_EXTENSION_NAME + ".config.generate.properties` is not empty,"
                        + " but `" + LOMBOK_EXTENSION_NAME + ".config.generate.enabled == null`."
                        + " If you want to generate Lombok config file, enable this functionality by calling"
                        + " `" + LOMBOK_EXTENSION_NAME + ".config.generate.enabled = true`."
                    );
                }
                return; // disabled by default
            }

            val generate = lombokExtension.getConfig().getGenerate();
            project.getTasks().register(GENERATE_LOMBOK_CONFIG_TASK_NAME, GenerateLombokConfig.class, task -> {
                task.getFile().convention(generate.getFile());
                task.getProperties().convention(generate.getProperties());
            });

            project.getTasks().withType(JavaCompile.class).configureEach(task -> {
                task.dependsOn(project.getTasks().withType(GenerateLombokConfig.class));
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
                delombok.dependsOn(javaCompileProvider);

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


    //#region Utilities

    private static String getDelombokTaskNameFor(SourceSet sourceSet) {
        return sourceSet.getTaskName(DELOMBOK_TASK_NAME, "");
    }

    private static void configureConfiguration(Project project, String name, Action<Configuration> action) {
        project.getConfigurations().matching(it -> Objects.equals(it.getName(), name)).all(action);
    }

    private static ExternalModuleDependency createDependency(Project project, LombokDependency lombokDependency) {
        return (ExternalModuleDependency) project.getDependencies().create(format(
            "%s:%s:%s",
            lombokDependency.getGroup(),
            lombokDependency.getName(),
            lombokDependency.getVersion()
        ));
    }

    private static ExternalModuleDependency createDependency(
        Project project,
        LombokDependency lombokDependency,
        @Nullable String version
    ) {
        if (isEmpty(version)) {
            return (ExternalModuleDependency) project.getDependencies().create(format(
                "%s:%s",
                lombokDependency.getGroup(),
                lombokDependency.getName()
            ));
        } else {
            return (ExternalModuleDependency) project.getDependencies().create(format(
                "%s:%s:%s",
                lombokDependency.getGroup(),
                lombokDependency.getName(),
                version
            ));
        }
    }

    @Inject
    protected abstract ProviderFactory getProviders();

    @Inject
    protected abstract ProjectLayout getProjectLayout();

    //#endregion

}
