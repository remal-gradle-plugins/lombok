package name.remal.gradle_plugins.lombok;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.readAllBytes;
import static name.remal.gradle_plugins.lombok.LibrariesToTestCompatibility.getLibraryNotation;
import static name.remal.gradle_plugins.toolkit.PathUtils.deleteRecursively;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.MinSupportedJavaVersion;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class LombokPluginFunctionalTest {

    private static final String APT_GENERATED_FOLDER = "build/apt-generated";

    private final GradleProject project;

    @BeforeEach
    void beforeEach() {
        project.forBuildFile(build -> {
            build.applyPlugin("name.remal.lombok");
            build.applyPlugin("java");
            build.addMavenCentralRepository();
            build.line("tasks.withType(JavaCompile) { options.compilerArgs.add('-parameters') }");
            build.line(join("\n", new String[]{
                "tasks.named('compileJava') {",
                "    options.generatedSourceOutputDirectory.fileValue(",
                "        project.file('" + build.escapeString(APT_GENERATED_FOLDER) + "')",
                "    )",
                "}"
            }));
        });

        project.writeTextFile("lombok.config", join(
            "\n",
            "config.stopBubbling = true"
        ));

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
    }

    private Path getGeneratedFile(String relativePath) {
        val path = project.getProjectDir().toPath()
            .resolve(relativePath);
        if (!exists(path)) {
            throw new AssertionError("File not found: " + path);
        }
        if (!isRegularFile(path)) {
            throw new AssertionError("Not a regular file: " + path);
        }
        return path;
    }

    private void assertGeneratedFileExists(String relativePath) {
        getGeneratedFile(relativePath);
    }


    @Test
    void compilation() {
        project.assertBuildSuccessfully("compileJava");
    }

    @Test
    void javadoc() {
        project.getBuildFile().line("java.withJavadocJar()");
        project.assertBuildSuccessfully("javadoc");
    }

    @Test
    void lombokConfigValidationFails() {
        project.assertBuildFails("validateLombokConfig");
    }

    @Test
    void lombokConfigValidationSucceeds() {
        project.getBuildFile().line("lombok.config.validate.disabledRules.addAll("
            + "'AddGeneratedAnnotation',"
            + "'ConfigureAccessorsUsage',"
            + "'ConfigureUtilityClassUsage',"
            + ")");
        project.assertBuildSuccessfully("validateLombokConfig");
    }

    @Test
    void generateConfig() {
        deleteRecursively(project.getProjectDir().toPath().resolve("lombok.config"));

        project.getBuildFile().line("lombok.config.generate { enabled = true; set('config.stopBubbling', true); }");
        project.assertBuildSuccessfully("compileJava");
    }


    @Test
    void doesNotConflictWithTransitiveDependencies() {
        project.getBuildFile().line("dependencies { implementation 'net.serenity-bdd:serenity-core:3.6.22' }");
        project.assertBuildSuccessfully("compileJava");
    }


    @Nested
    class Compatibilities {

        @Test
        void mapstruct() throws Throwable {
            project.forBuildFile(build -> {
                build.line(format("dependencies { compileOnly '%s' }", build.escapeString(getLibraryNotation(
                    "org.mapstruct:mapstruct"
                ))));
                build.line(format("dependencies { annotationProcessor '%s' }", build.escapeString(getLibraryNotation(
                    "org.mapstruct:mapstruct-processor"
                ))));
            });

            project.writeTextFile("src/main/java/pkg/TestClassWithBuilder.java", join(
                "\n",
                "package pkg;",
                "",
                "import lombok.Value;",
                "import lombok.Builder;",
                "",
                "@Value",
                "@Builder",
                "public class TestClassWithBuilder {",
                "    String field;",
                "}"
            ));

            project.writeTextFile("src/main/java/pkg/TestClassMapper.java", join(
                "\n",
                "package pkg;",
                "",
                "import org.mapstruct.Mapper;",
                "",
                "@Mapper",
                "public interface TestClassMapper {",
                "    TestClassWithBuilder map(TestClass object);",
                "}"
            ));

            project.assertBuildSuccessfully("compileJava");

            val contentBytes = readAllBytes(getGeneratedFile(APT_GENERATED_FOLDER + "/pkg/TestClassMapperImpl.java"));
            val content = new String(contentBytes, UTF_8);
            assertThat(content).contains("TestClassWithBuilder.builder()");
        }

        @Test
        @MinSupportedJavaVersion(17)
        void micronaut() {
            project.getBuildFile().block("dependencies", deps -> {
                deps.line(
                    "compileOnly '%s'",
                    deps.escapeString(getLibraryNotation("io.micronaut.validation:micronaut-validation"))
                );
                deps.line(
                    "annotationProcessor '%s'",
                    deps.escapeString(getLibraryNotation("io.micronaut.validation:micronaut-validation-processor"))
                );
                deps.line(
                    "annotationProcessor '%s'",
                    deps.escapeString(getLibraryNotation("io.micronaut:micronaut-inject-java"))
                );
            });

            project.writeTextFile("src/main/java/pkg/TestClassValidated.java", join(
                "\n",
                "package pkg;",
                "",
                "import jakarta.inject.Singleton;",
                "import jakarta.validation.constraints.NotEmpty;",
                "import io.micronaut.core.annotation.Introspected;",
                "import lombok.Data;",
                "import lombok.Setter;",
                "",
                "@Singleton",
                "@Data",
                "@Introspected",
                "public class TestClassValidated {",
                "    @Setter(onParam_ = {@NotEmpty})",
                "    String field;",
                "}"
            ));

            project.assertBuildSuccessfully("compileJava");

            assertGeneratedFileExists("build/classes/java/main/pkg/$TestClassValidated$Definition$Exec.class");
        }

    }

}
