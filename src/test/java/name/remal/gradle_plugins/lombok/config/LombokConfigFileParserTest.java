package name.remal.gradle_plugins.lombok.config;

import static java.lang.String.join;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.CLEAR;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.MINUS;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.PLUS;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.SET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;
import org.junit.jupiter.api.Test;

class LombokConfigFileParserTest {

    final LombokConfigPath file = mock(LombokConfigPath.class);

    @Test
    void invalidLine() {
        when(file.readContent()).thenReturn(join(
            "\n",
            " # comment",
            "importgg"
        ));

        val configFile = LombokConfigFileParser.parseLombokConfigFileImpl(file);
        assertThat(configFile.getImportInstructions())
            .as("importInstruction")
            .isEmpty();

        assertThat(configFile.getProperties())
            .as("properties")
            .isEmpty();

        assertThat(configFile.getParseErrors())
            .as("parseErrors")
            .containsExactly(LombokConfigFileParseError.builder()
                .file(file)
                .lineNumber(2)
                .message("Invalid line: importgg")
                .build()
            );
    }

    @Test
    void importStatement() {
        when(file.readContent()).thenReturn(join(
            "\n",
            " # comment",
            "import gg"
        ));

        val configFile = LombokConfigFileParser.parseLombokConfigFileImpl(file);
        assertThat(configFile.getImportInstructions())
            .as("importInstruction")
            .containsExactly(ImportInstruction.builder()
                .file(file)
                .lineNumber(2)
                .value("gg")
                .build()
            );

        assertThat(configFile.getProperties())
            .as("properties")
            .isEmpty();

        assertThat(configFile.getParseErrors())
            .as("parseErrors")
            .isEmpty();
    }

    @Test
    void invalidOperator() {
        when(file.readContent()).thenReturn(join(
            "\n",
            " # comment",
            "key ~= value"
        ));

        assertThat(LombokConfigFileParser.parseLombokConfigFileImpl(file)).satisfies(configFile -> {
            assertThat(configFile.getImportInstructions())
                .as("importInstruction")
                .isEmpty();

            assertThat(configFile.getProperties())
                .as("properties")
                .isEmpty();

            assertThat(configFile.getParseErrors())
                .as("parseErrors")
                .containsExactly(LombokConfigFileParseError.builder()
                    .file(file)
                    .lineNumber(2)
                    .message("Invalid line: key ~= value")
                    .build()
                );
        });
    }

    @Test
    void setOperator() {
        when(file.readContent()).thenReturn(join(
            "\n",
            " # comment",
            "key = value"
        ));

        assertThat(LombokConfigFileParser.parseLombokConfigFileImpl(file)).satisfies(configFile -> {
            assertThat(configFile.getImportInstructions())
                .as("importInstruction")
                .isEmpty();

            assertThat(configFile.getProperties())
                .as("properties")
                .containsExactly(LombokConfigFileProperty.builder()
                    .file(file)
                    .lineNumber(2)
                    .key("key")
                    .operator(SET)
                    .value("value")
                    .build()
                );

            assertThat(configFile.getParseErrors())
                .as("parseErrors")
                .isEmpty();
        });
    }

    @Test
    void plusOperator() {
        when(file.readContent()).thenReturn(join(
            "\n",
            " # comment",
            "key += value"
        ));

        assertThat(LombokConfigFileParser.parseLombokConfigFileImpl(file)).satisfies(configFile -> {
            assertThat(configFile.getImportInstructions())
                .as("importInstruction")
                .isEmpty();

            assertThat(configFile.getProperties())
                .as("properties")
                .containsExactly(LombokConfigFileProperty.builder()
                    .file(file)
                    .lineNumber(2)
                    .key("key")
                    .operator(PLUS)
                    .value("value")
                    .build()
                );

            assertThat(configFile.getParseErrors())
                .as("parseErrors")
                .isEmpty();
        });
    }

    @Test
    void minusOperator() {
        when(file.readContent()).thenReturn(join(
            "\n",
            " # comment",
            "key -= value"
        ));

        assertThat(LombokConfigFileParser.parseLombokConfigFileImpl(file)).satisfies(configFile -> {
            assertThat(configFile.getImportInstructions())
                .as("importInstruction")
                .isEmpty();

            assertThat(configFile.getProperties())
                .as("properties")
                .containsExactly(LombokConfigFileProperty.builder()
                    .file(file)
                    .lineNumber(2)
                    .key("key")
                    .operator(MINUS)
                    .value("value")
                    .build()
                );

            assertThat(configFile.getParseErrors())
                .as("parseErrors")
                .isEmpty();
        });
    }

    @Test
    void clearOperator() {
        when(file.readContent()).thenReturn(join(
            "\n",
            " # comment",
            "clear key"
        ));

        assertThat(LombokConfigFileParser.parseLombokConfigFileImpl(file)).satisfies(configFile -> {
            assertThat(configFile.getImportInstructions())
                .as("importInstruction")
                .isEmpty();

            assertThat(configFile.getProperties())
                .as("properties")
                .containsExactly(LombokConfigFileProperty.builder()
                    .file(file)
                    .lineNumber(2)
                    .key("key")
                    .operator(CLEAR)
                    .value("")
                    .build()
                );

            assertThat(configFile.getParseErrors())
                .as("parseErrors")
                .isEmpty();
        });
    }

    @Test
    void invalidClearOperator() {
        when(file.readContent()).thenReturn(join(
            "\n",
            " # comment",
            "clear key = value"
        ));

        assertThat(LombokConfigFileParser.parseLombokConfigFileImpl(file)).satisfies(configFile -> {
            assertThat(configFile.getImportInstructions())
                .as("importInstruction")
                .isEmpty();

            assertThat(configFile.getProperties())
                .as("properties")
                .isEmpty();

            assertThat(configFile.getParseErrors())
                .as("parseErrors")
                .containsExactly(LombokConfigFileParseError.builder()
                    .file(file)
                    .lineNumber(2)
                    .message("Invalid line: clear key = value")
                    .build()
                );
        });
    }

    @Test
    void keyNormalization() {
        when(file.readContent()).thenReturn(join(
            "\n",
            "lombok.addlombokgeneratedannotation = true"
        ));

        assertThat(LombokConfigFileParser.parseLombokConfigFileImpl(file)).satisfies(configFile -> {
            assertThat(configFile.getImportInstructions())
                .as("importInstruction")
                .isEmpty();

            assertThat(configFile.getProperties())
                .as("properties")
                .containsExactly(LombokConfigFileProperty.builder()
                    .file(file)
                    .lineNumber(1)
                    .key("lombok.addLombokGeneratedAnnotation")
                    .operator(SET)
                    .value("true")
                    .build()
                );

            assertThat(configFile.getParseErrors())
                .as("parseErrors")
                .isEmpty();
        });
    }

}
