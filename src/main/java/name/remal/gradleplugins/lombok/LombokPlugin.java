package name.remal.gradleplugins.lombok;

import static java.lang.String.format;
import static name.remal.gradleplugins.lombok.LombokDependencies.getLombokDependency;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradleplugins.toolkit.ObjectUtils.doNotInline;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;

public class LombokPlugin implements Plugin<Project> {

    public static final String LOMBOK_EXTENSION_NAME = doNotInline("lombok");
    public static final String LOMBOK_CONFIGURATION_NAME = doNotInline("lombok");

    @Override
    public void apply(Project project) {
        LombokExtension lombokExtension = project.getExtensions().create(LOMBOK_EXTENSION_NAME, LombokExtension.class);

        Configuration lombokConf = project.getConfigurations().create(LOMBOK_CONFIGURATION_NAME, conf -> {
            conf.setDescription("Lombok");
            conf.defaultDependencies(deps -> {
                deps.add(createDependency(
                    project,
                    getLombokDependency("lombok"),
                    lombokExtension.getLombokVersion().get()
                ));
            });
        });

        project.getPluginManager().withPlugin("java", __ -> configureJavaProject(
            project,
            lombokConf
        ));
    }


    private static void configureJavaProject(
        Project project,
        Configuration lombokConf
    ) {
        SourceSetContainer sourceSets = getExtension(project, JavaPluginExtension.class).getSourceSets();

        sourceSets.all(sourceSet -> {
            configureConfiguration(project, sourceSet.getCompileOnlyConfigurationName(), conf -> {
                conf.extendsFrom(lombokConf);
            });

            configureConfiguration(project, sourceSet.getAnnotationProcessorConfigurationName(), conf -> {
                conf.getDependencies().add(createDependency(
                    project,
                    getLombokDependency("lombok-mapstruct-binding")
                ));

                conf.extendsFrom(lombokConf);
                AtomicBoolean isInUpdating = new AtomicBoolean(false);
                conf.getAllDependencies().whenObjectAdded(__ -> {
                    if (isInUpdating.compareAndSet(false, true)) {
                        try {
                            val extendsFrom = new LinkedHashSet<>(conf.getExtendsFrom());
                            extendsFrom.remove(lombokConf);
                            extendsFrom.add(lombokConf);
                            conf.setExtendsFrom(extendsFrom);
                        } finally {
                            isInUpdating.set(false);
                        }
                    }
                });
            });
        });
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
