package name.remal.gradle_plugins.lombok.config;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.writeString;
import static name.remal.gradle_plugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.CLEAR;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.MINUS;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.PLUS;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.SET;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LombokConfigTest extends AbstractLombokConfigTest {

    final Path configA = createParentDirectories(fs.getPath("/a").resolve(LOMBOK_CONFIG_FILE_NAME));
    final Path configAb = createParentDirectories(fs.getPath("/a/b").resolve(LOMBOK_CONFIG_FILE_NAME));
    final Path configAbc = createParentDirectories(fs.getPath("/a/b/c").resolve(LOMBOK_CONFIG_FILE_NAME));
    final Path configX = createParentDirectories(fs.getPath("/x").resolve(LOMBOK_CONFIG_FILE_NAME));
    final Path configY = createParentDirectories(fs.getPath("/y").resolve(LOMBOK_CONFIG_FILE_NAME));
    final Path configZ = createParentDirectories(fs.getPath("/z").resolve(LOMBOK_CONFIG_FILE_NAME));

    @Test
    void simpleStopBubbling() throws Throwable {
        writeString(configA, join(
            "\n",
            ""
        ), UTF_8);

        writeString(configAb, join(
            "\n",
            "config.stopBubbling = true"
        ), UTF_8);

        writeString(configAbc, join(
            "\n",
            "config.stopBubbling = false"
        ), UTF_8);

        var lombokConfig = new LombokConfig(configAbc);
        assertThat(lombokConfig.getInvolvedPaths())
            .as("involvedPaths")
            .containsExactly(
                configAb,
                configAbc
            );
    }

    @Test
    void simpleImport() throws Throwable {
        writeString(configA, join(
            "\n",
            "import " + configX,
            "import " + configY,
            "import " + configZ
        ), UTF_8);

        writeString(configX, join(
            "\n",
            ""
        ), UTF_8);

        writeString(configY, join(
            "\n",
            ""
        ), UTF_8);

        writeString(configZ, join(
            "\n",
            ""
        ), UTF_8);

        var lombokConfig = new LombokConfig(configA);
        assertThat(lombokConfig.getInvolvedPaths())
            .as("involvedPaths")
            .containsExactly(
                configX,
                configY,
                configZ,
                configA
            );
    }

    @Test
    void importStopBubbling() throws Throwable {
        writeString(configA, join(
            "\n",
            ""
        ), UTF_8);

        writeString(configAb, join(
            "\n",
            "import " + configY,
            "import " + configZ
        ), UTF_8);

        writeString(configAbc, join(
            "\n",
            ""
        ), UTF_8);

        writeString(configY, join(
            "\n",
            "config.stopBubbling = true"
        ), UTF_8);

        writeString(configZ, join(
            "\n",
            ""
        ), UTF_8);

        var lombokConfig = new LombokConfig(configAbc);
        assertThat(lombokConfig.getInvolvedPaths())
            .as("involvedPaths")
            .containsExactly(
                configY,
                configZ,
                configAb,
                configAbc
            );
    }


    @Test
    void get() throws Throwable {
        writeString(configA, join(
            "\n",
            "prop.a = a",
            "prop.a.b = a",
            "prop.a.b.c = a"
        ), UTF_8);

        writeString(configAb, join(
            "\n",
            "config.stopBubbling = true",
            "prop.a.b = ab",
            "prop.a.b.c = ab",
            "prop.x = x",
            "prop.x = xxx"
        ), UTF_8);

        writeString(configAbc, join(
            "\n",
            "prop.a.b.c = abc"
        ), UTF_8);

        var lombokConfig = new LombokConfig(configAbc);
        assertThat(lombokConfig.get("prop.a"))
            .as("prop.a")
            .isNull();

        assertThat(lombokConfig.get("prop.a.b"))
            .as("prop.a.b")
            .isEqualTo("ab");

        assertThat(lombokConfig.get("prop.a.b.c"))
            .as("prop.a.b.c")
            .isEqualTo("abc");

        assertThat(lombokConfig.get("prop.x"))
            .as("prop.x")
            .isEqualTo("xxx");
    }

    @Test
    void getList() throws Throwable {
        writeString(configA, join(
            "\n",
            "list += a"
        ), UTF_8);

        writeString(configAb, join(
            "\n",
            "config.stopBubbling = true",
            "list += ab",
            "list += ba",
            "list -= ba"
        ), UTF_8);

        writeString(configAbc, join(
            "\n",
            "list += xxx",
            "clear list",
            "list += abc",
            "list -= cba",
            "list += cba",
            "list -= cba"
        ), UTF_8);

        var lombokConfig = new LombokConfig(configAbc);
        assertThat(lombokConfig.getList("list"))
            .as("list")
            .containsExactly(
                "abc"
            );
    }

    @Test
    void clear() throws Throwable {
        writeString(configA, join(
            "\n",
            "list += a",
            "prop += a"
        ), UTF_8);

        writeString(configAb, join(
            "\n",
            "clear list",
            "clear prop"
        ), UTF_8);

        var lombokConfig = new LombokConfig(configAbc);
        assertThat(lombokConfig.getList("list"))
            .as("list")
            .isEmpty();
        assertThat(lombokConfig.get("prop"))
            .as("prop")
            .isNull();
    }

    @Test
    void getAllProperties() throws Throwable {
        writeString(configA, join(
            "\n",
            "prop = a",
            "list += a"
        ), UTF_8);

        writeString(configAb, join(
            "\n",
            "config.stopBubbling = true",
            "prop = b",
            "list += ab",
            "list += ba",
            "list -= ba"
        ), UTF_8);

        writeString(configAbc, join(
            "\n",
            "prop = c",
            "clear list",
            "list += abc"
        ), UTF_8);

        var lombokConfig = new LombokConfig(configAbc);
        assertThat(lombokConfig.getProperties())
            .as("allProperties")
            .containsExactly(
                LombokConfigFileProperty.builder()
                    .file(new LombokConfigPathSystem(configAb))
                    .lineNumber(1)
                    .key("config.stopBubbling")
                    .operator(SET)
                    .value("true")
                    .build(),
                LombokConfigFileProperty.builder()
                    .file(new LombokConfigPathSystem(configAb))
                    .lineNumber(2)
                    .key("prop")
                    .operator(SET)
                    .value("b")
                    .build(),
                LombokConfigFileProperty.builder()
                    .file(new LombokConfigPathSystem(configAb))
                    .lineNumber(3)
                    .key("list")
                    .operator(PLUS)
                    .value("ab")
                    .build(),
                LombokConfigFileProperty.builder()
                    .file(new LombokConfigPathSystem(configAb))
                    .lineNumber(4)
                    .key("list")
                    .operator(PLUS)
                    .value("ba")
                    .build(),
                LombokConfigFileProperty.builder()
                    .file(new LombokConfigPathSystem(configAb))
                    .lineNumber(5)
                    .key("list")
                    .operator(MINUS)
                    .value("ba")
                    .build(),

                LombokConfigFileProperty.builder()
                    .file(new LombokConfigPathSystem(configAbc))
                    .lineNumber(1)
                    .key("prop")
                    .operator(SET)
                    .value("c")
                    .build(),
                LombokConfigFileProperty.builder()
                    .file(new LombokConfigPathSystem(configAbc))
                    .lineNumber(2)
                    .key("list")
                    .operator(CLEAR)
                    .value("")
                    .build(),
                LombokConfigFileProperty.builder()
                    .file(new LombokConfigPathSystem(configAbc))
                    .lineNumber(3)
                    .key("list")
                    .operator(PLUS)
                    .value("abc")
                    .build()
            );
    }

}
