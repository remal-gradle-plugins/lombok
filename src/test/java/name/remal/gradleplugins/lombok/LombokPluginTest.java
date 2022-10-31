package name.remal.gradleplugins.lombok;

import static java.util.stream.Collectors.toUnmodifiableList;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;

import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class LombokPluginTest {

    final Project project;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply(LombokPlugin.class);
        project.getPluginManager().apply("java");
        project.getRepositories().mavenCentral();
    }


    @Nested
    class LombokDependencyAlwaysAtTheEnd {

        @Test
        void simple() {
            val annotationProcessorConf = getAnnotationProcessorConf();

            val resolvedModules = annotationProcessorConf.getResolvedConfiguration()
                .getLenientConfiguration()
                .getAllModuleDependencies();
            assertThat(resolvedModules).extracting(ResolvedDependency::getModuleName).containsExactlyInAnyOrder(
                "lombok-mapstruct-binding",
                "lombok"
            );

            val fileNames = annotationProcessorConf.getResolvedConfiguration()
                .getLenientConfiguration()
                .getFiles()
                .stream()
                .map(File::getName)
                .map(fileName -> fileName.replaceFirst("-\\d.*", ""))
                .collect(toUnmodifiableList());
            assertThat(fileNames).containsExactlyInAnyOrder(
                "lombok-mapstruct-binding",
                "lombok"
            );
        }

        @Test
        void addDependency() {
            val annotationProcessorConf = getAnnotationProcessorConf();

            annotationProcessorConf.getDependencies().add(project.getDependencies().create(
                "org.mapstruct:mapstruct-processor:1.5.3.Final"
            ));

            val resolvedModules = annotationProcessorConf.getResolvedConfiguration()
                .getLenientConfiguration()
                .getAllModuleDependencies();
            assertThat(resolvedModules).extracting(ResolvedDependency::getModuleName).containsExactlyInAnyOrder(
                "lombok-mapstruct-binding",
                "mapstruct-processor",
                "lombok"
            );

            val fileNames = annotationProcessorConf.getResolvedConfiguration()
                .getLenientConfiguration()
                .getFiles()
                .stream()
                .map(File::getName)
                .map(fileName -> fileName.replaceFirst("-\\d.*", ""))
                .collect(toUnmodifiableList());
            assertThat(fileNames).containsExactlyInAnyOrder(
                "lombok-mapstruct-binding",
                "mapstruct-processor",
                "lombok"
            );
        }

        @Test
        void extendWithDependency() {
            val mapstructConf = project.getConfigurations().create("mapstruct");

            mapstructConf.getDependencies().add(project.getDependencies().create(
                "org.mapstruct:mapstruct-processor:1.5.3.Final"
            ));

            val annotationProcessorConf = getAnnotationProcessorConf();

            annotationProcessorConf.extendsFrom(mapstructConf);

            val resolvedModules = annotationProcessorConf.getResolvedConfiguration()
                .getLenientConfiguration()
                .getAllModuleDependencies();
            assertThat(resolvedModules).extracting(ResolvedDependency::getModuleName).containsExactlyInAnyOrder(
                "lombok-mapstruct-binding",
                "mapstruct-processor",
                "lombok"
            );

            val fileNames = annotationProcessorConf.getResolvedConfiguration()
                .getLenientConfiguration()
                .getFiles()
                .stream()
                .map(File::getName)
                .map(fileName -> fileName.replaceFirst("-\\d.*", ""))
                .collect(toUnmodifiableList());
            assertThat(fileNames).containsExactlyInAnyOrder(
                "lombok-mapstruct-binding",
                "mapstruct-processor",
                "lombok"
            );
        }

        @Test
        void extendAndAddDependency() {
            val mapstructConf = project.getConfigurations().create("mapstruct");

            val annotationProcessorConf = getAnnotationProcessorConf();

            annotationProcessorConf.extendsFrom(mapstructConf);

            mapstructConf.getDependencies().add(project.getDependencies().create(
                "org.mapstruct:mapstruct-processor:1.5.3.Final"
            ));

            val resolvedModules = annotationProcessorConf.getResolvedConfiguration()
                .getLenientConfiguration()
                .getAllModuleDependencies();
            assertThat(resolvedModules).extracting(ResolvedDependency::getModuleName).containsExactlyInAnyOrder(
                "lombok-mapstruct-binding",
                "mapstruct-processor",
                "lombok"
            );

            val fileNames = annotationProcessorConf.getResolvedConfiguration()
                .getLenientConfiguration()
                .getFiles()
                .stream()
                .map(File::getName)
                .map(fileName -> fileName.replaceFirst("-\\d.*", ""))
                .collect(toUnmodifiableList());
            assertThat(fileNames).containsExactlyInAnyOrder(
                "lombok-mapstruct-binding",
                "mapstruct-processor",
                "lombok"
            );
        }

    }


    private Configuration getAnnotationProcessorConf() {
        SourceSet sourceSet = getExtension(project, JavaPluginExtension.class).getSourceSets()
            .getByName(MAIN_SOURCE_SET_NAME);

        return project.getConfigurations().getByName(
            sourceSet.getAnnotationProcessorConfigurationName()
        );
    }

}
