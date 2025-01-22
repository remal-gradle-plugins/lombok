package name.remal.gradle_plugins.lombok;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static name.remal.gradle_plugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorFilesOrder;
import static name.remal.gradle_plugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorsOrder;
import static name.remal.gradle_plugins.lombok.JavacPackagesToOpenUtils.shouldJavacPackageOpenJvmArgsBeAdded;
import static name.remal.gradle_plugins.lombok.JavacPackagesToOpenUtils.withJavacPackageOpens;
import static name.remal.gradle_plugins.lombok.LombokDependencies.getLombokDependency;
import static name.remal.gradle_plugins.lombok.config.LombokConfigUtils.parseLombokConfigs;
import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.defaultTrue;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.doNotInline;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.ProjectUtils.afterEvaluateOrNow;
import static name.remal.gradle_plugins.toolkit.TaskUtils.doBeforeTaskExecution;
import static org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE;
import static org.gradle.api.attributes.Category.LIBRARY;
import static org.gradle.api.attributes.Usage.JAVA_API;
import static org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE;
import static org.gradle.api.plugins.JavaBasePlugin.CHECK_TASK_NAME;
import static org.gradle.api.plugins.JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME;
import static org.gradle.api.plugins.JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME;
import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.CustomLog;
import name.remal.gradle_plugins.lombok.config.GenerateLombokConfig;
import name.remal.gradle_plugins.lombok.config.LombokConfig;
import name.remal.gradle_plugins.lombok.config.LombokConfigUtils;
import name.remal.gradle_plugins.lombok.config.ValidateLombokConfig;
import name.remal.gradle_plugins.toolkit.JavaInstallationMetadataUtils;
import name.remal.gradle_plugins.toolkit.ObjectUtils;
import name.remal.gradle_plugins.toolkit.Version;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.attributes.AttributeContainer;
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

        this.lombokConf = project.getConfigurations().create(LOMBOK_CONFIGURATION_NAME, conf -> {
            conf.setDescription("Lombok");
            conf.defaultDependencies(deps -> {
                deps.add(createDependency(
                    getLombokDependency("lombok"),
                    lombokExtension.getLombokVersion().getOrNull()
                ));
            });

            conf.setCanBeConsumed(false);
            conf.attributes(javaRuntimeLibrary());
        });

        lombokExtension.getLombokVersion().convention(getProviders().provider(this::getDefaultLombokVersion));


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


    private String getDefaultLombokVersion() {
        var lombokDependency = getLombokDependency("lombok");
        return Stream.of(
                lombokConf,
                project.getPluginManager().hasPlugin("java")
                    ? project.getConfigurations().findByName(ANNOTATION_PROCESSOR_CONFIGURATION_NAME)
                    : null,
                project.getPluginManager().hasPlugin("java")
                    ? project.getConfigurations().findByName(COMPILE_CLASSPATH_CONFIGURATION_NAME)
                    : null
            )
            .filter(Objects::nonNull)
            .map(Configuration::getAllDependencyConstraints)
            .map(constraints -> constraints.stream()
                .filter(constraint -> lombokDependency.getGroup().equals(constraint.getGroup()))
                .filter(constraint -> lombokDependency.getName().equals(constraint.getName()))
                .map(DependencyConstraint::getVersion)
                .filter(ObjectUtils::isNotEmpty)
                .reduce((first, second) -> Version.parse(first).compareTo(Version.parse(second)) >= 0 ? first : second)
                .orElse(null)
            )
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(lombokDependency::getVersion);
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
        var isEnabled = lombokExtension.getFixJavacReflectionsAccess();
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            doBeforeTaskExecution(task, __ -> {
                var compileOptions = task.getOptions();
                if (compileOptions == null) {
                    return;
                }

                if (!defaultTrue(isEnabled.getOrNull())) {
                    return;
                }

                var sourceCompatibility = Optional.ofNullable(task.getSourceCompatibility())
                    .map(JavaVersion::toVersion)
                    .orElse(null);
                if (sourceCompatibility != null && !shouldJavacPackageOpenJvmArgsBeAdded(sourceCompatibility)) {
                    return;
                }

                var targetCompatibility = Optional.ofNullable(task.getTargetCompatibility())
                    .map(JavaVersion::toVersion)
                    .orElse(null);
                if (targetCompatibility != null && !shouldJavacPackageOpenJvmArgsBeAdded(targetCompatibility)) {
                    return;
                }

                var compilerJavaVersion = Optional.ofNullable(task.getJavaCompiler())
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
        var isEnabled = lombokExtension.getFixAnnotationProcessorsOrder();
        var layout = getLayout();
        var providers = getProviders();
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            doBeforeTaskExecution(task, __ -> {
                var compileOptions = task.getOptions();
                if (compileOptions == null) {
                    return;
                }

                if (!defaultTrue(isEnabled.getOrNull())) {
                    return;
                }

                var annotationProcessorPath = compileOptions.getAnnotationProcessorPath();
                if (annotationProcessorPath == null) {
                    return;
                }

                compileOptions.setAnnotationProcessorPath(layout.files(providers.provider(() -> {
                    Collection<File> files = annotationProcessorPath.getFiles();
                    files = withFixedAnnotationProcessorFilesOrder(files);
                    return files;
                })));
            });

            doBeforeTaskExecution(task, __ -> {
                var compileOptions = task.getOptions();
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
                var processors = Splitter.on(',').splitToStream(processorsArg)
                    .map(String::trim)
                    .filter(ObjectUtils::isNotEmpty)
                    .collect(toList());
                if (processors.isEmpty()) {
                    return;
                }

                var fixedProcessors = withFixedAnnotationProcessorsOrder(processors);
                processorsArg = Joiner.on(',').join(fixedProcessors);

                compilerArgs = new ArrayList<>(compilerArgs);
                compilerArgs.set(processorIndex + 1, processorsArg);
                compileOptions.setCompilerArgs(compilerArgs);
            });
        });
    }


    private void configureCompileInputFiles() {
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            task.getInputs().files(getProviders().provider(() ->
                parseLombokConfigs(task).stream()
                    .map(LombokConfig::getInvolvedPaths)
                    .flatMap(Collection::stream)
                    .collect(toSet())
            )).optional(true).withPathSensitivity(RELATIVE);
        });
    }


    private void configureConfigValidation() {
        var tasks = project.getTasks();
        var extensionDisabledRules = lombokExtension.getConfig().getValidate().getDisabledRules();
        tasks.register(VALIDATE_LOMBOK_CONFIG_TASK_NAME, ValidateLombokConfig.class, task -> {
            var javaCompileTasks = tasks.withType(JavaCompile.class);
            task.dependsOn(javaCompileTasks);

            task.getDirectories().from(getLayout().getProjectDirectory());
            task.getDirectories().from(getProviders().provider(() ->
                javaCompileTasks.stream()
                    .flatMap(LombokConfigUtils::streamJavaCompileSourceDirs)
                    .collect(toSet())
            ));

            task.getDisabledRules().convention(extensionDisabledRules);
        });

        project.getPluginManager().withPlugin("java", __ -> {
            tasks.named(CHECK_TASK_NAME, task -> {
                task.dependsOn(tasks.withType(ValidateLombokConfig.class));
            });
        });
    }


    @SuppressWarnings({"Slf4jFormatShouldBeConst", "StringConcatenationArgumentToLogCall"})
    private void configureConfigGeneration() {
        afterEvaluateOrNow(project, __ -> {
            var isEnabled = lombokExtension.getConfig().getGenerate().getEnabled().getOrNull();
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

            var generate = lombokExtension.getConfig().getGenerate();
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
                    getLombokDependency("lombok-mapstruct-binding")
                ));
            });
        });
    }


    private void configureDelombokForAllSourceSets() {
        getExtension(project, SourceSetContainer.class).all(sourceSet -> {
            var delombokTaskName = getDelombokTaskNameFor(sourceSet);
            var delombokProvider = project.getTasks().register(delombokTaskName, Delombok.class, delombok -> {
                delombok.dependsOn(sourceSet.getClassesTaskName());

                var javaCompileProvider = project.getTasks().named(
                    sourceSet.getCompileJavaTaskName(),
                    JavaCompile.class
                );
                delombok.dependsOn(javaCompileProvider);

                delombok.getEncoding().set(getProviders().provider(() ->
                    javaCompileProvider.get().getOptions().getEncoding()
                ));

                delombok.getClasspath().setFrom(getProviders().provider(() ->
                    javaCompileProvider.get().getClasspath()
                ));

                delombok.getInputFiles().setFrom(sourceSet.getAllJava().getSourceDirectories());
                delombok.getInputFiles().from(getProviders().provider(() ->
                    javaCompileProvider.get().getOptions().getGeneratedSourceOutputDirectory()
                ));
            });

            var javadocTaskName = sourceSet.getJavadocTaskName();
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

    private ExternalModuleDependency createDependency(LombokDependency lombokDependency) {
        var dep = (ExternalModuleDependency) project.getDependencies().create(format(
            "%s:%s:%s",
            lombokDependency.getGroup(),
            lombokDependency.getName(),
            lombokDependency.getVersion()
        ));
        dep.attributes(javaRuntimeLibrary());
        return dep;
    }

    private ExternalModuleDependency createDependency(
        LombokDependency lombokDependency,
        @Nullable String version
    ) {
        final ExternalModuleDependency dep;
        if (isEmpty(version)) {
            dep = (ExternalModuleDependency) project.getDependencies().create(format(
                "%s:%s",
                lombokDependency.getGroup(),
                lombokDependency.getName()
            ));
        } else {
            dep = (ExternalModuleDependency) project.getDependencies().create(format(
                "%s:%s:%s",
                lombokDependency.getGroup(),
                lombokDependency.getName(),
                version
            ));
        }
        dep.attributes(javaRuntimeLibrary());
        return dep;
    }

    private Action<AttributeContainer> javaRuntimeLibrary() {
        return attrs -> {
            attrs.attribute(
                USAGE_ATTRIBUTE,
                project.getObjects().named(Usage.class, JAVA_API)
            );
            attrs.attribute(
                CATEGORY_ATTRIBUTE,
                project.getObjects().named(Category.class, LIBRARY)
            );
        };
    }

    @Inject
    protected abstract ProviderFactory getProviders();

    @Inject
    protected abstract ProjectLayout getLayout();

    //#endregion

}
