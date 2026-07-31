package name.remal.gradle_plugins.lombok;

import static java.lang.String.join;
import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.MavenCentralRepositoryUtils.addMavenCentralRepository;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class LombokPluginAppliedViaSettingsFunctionalTest {

    private final GradleProject project;

    @Test
    void appliedViaSettingsIsAppliedToProject() {
        project.forSettingsFile(settings -> {
            settings.applyPlugin("name.remal.lombok");
            settings.block("dependencyResolutionManagement", dependencyResolutionManagement -> {
                addMavenCentralRepository(dependencyResolutionManagement);
            });
        });

        // The plugin must NOT be applied via the project's build file: it should reach the project
        // solely through the Settings-level application propagating via GradleLifecycle.beforeProject.
        project.forBuildFile(build -> build.applyPlugin("java"));

        project.writeTextFile("src/main/java/pkg/TestClass.java", join(
            "\n",
            "package pkg;",
            "",
            "import lombok.Data;",
            "",
            "@Data",
            "public class TestClass {",
            "    private String field;",
            "}"
        ));

        project.assertBuildSuccessfully("compileJava");
    }

}
