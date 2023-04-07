package name.remal.gradle_plugins.lombok;

import static java.lang.String.join;
import static name.remal.gradle_plugins.toolkit.PathUtils.deleteRecursively;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class LombokPluginFunctionalTest {

    private final GradleProject project;

    @BeforeEach
    void beforeEach() {
        project.forBuildFile(build -> {
            build.applyPlugin("name.remal.lombok");
            build.applyPlugin("java");
            build.append("repositories { mavenCentral() }");
        });

        project.writeTextFile("lombok.config", join(
            "\n",
            "config.stopBubbling = true"
        ));

        project.writeTextFile("src/main/java/pkg/TestClass.java", join(
            "\n",
            "package pkg;",
            "",
            "import lombok.Value;",
            "",
            "@Value",
            "public class TestClass {",
            "    String field;",
            "}"
        ));

        project.withoutConfigurationCache();
    }

    @Test
    void compilation() {
        project.getBuildFile()
            .registerDefaultTask("compileJava");
        project.assertBuildSuccessfully();
    }

    @Test
    void javadoc() {
        project.getBuildFile()
            .append("java.withJavadocJar()")
            .registerDefaultTask("javadoc");
        project.assertBuildSuccessfully();
    }

    @Test
    void lombokConfigValidationFails() {
        project.getBuildFile()
            .registerDefaultTask("validateLombokConfig");
        project.assertBuildFails();
    }

    @Test
    void lombokConfigValidationSucceeds() {
        project.getBuildFile()
            .append("lombok.config.validate.disabledRules.addAll("
                + "'AddGeneratedAnnotation',"
                + "'ConfigureAccessorsUsage',"
                + "'ConfigureUtilityClassUsage',"
                + ")")
            .registerDefaultTask("validateLombokConfig");
        project.assertBuildSuccessfully();
    }

    @Test
    void generateConfig() {
        deleteRecursively(project.getProjectDir().toPath().resolve("lombok.config"));

        project.getBuildFile()
            .append("lombok.config.generate { enabled = true; set('config.stopBubbling', true); }")
            .registerDefaultTask("compileJava");
        project.assertBuildSuccessfully();
    }


    @Test
    void doesNotConflictWithTransitiveDependencies() {
        project.getBuildFile()
            .append("dependencies { implementation 'net.serenity-bdd:serenity-core:3.6.22' }")
            .registerDefaultTask("compileJava");
        project.assertBuildSuccessfully();
    }

}
