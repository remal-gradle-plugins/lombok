package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.writeString;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static name.remal.gradle_plugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.reflect.TypeToken;
import com.google.errorprone.annotations.CheckReturnValue;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import name.remal.gradle_plugins.lombok.config.AbstractLombokConfigTest;
import name.remal.gradle_plugins.lombok.config.LombokConfig;
import org.assertj.core.api.CollectionAssert;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

abstract class AbstractLombokConfigRuleTest<Rule extends AbstractRule>
    extends AbstractLombokConfigTest {

    protected final Rule rule;


    protected final Path rootDir;

    protected final Path rootLombokConfig;


    protected final Path projectDir;

    protected final Path projectLombokConfig;


    @SneakyThrows
    protected AbstractLombokConfigRuleTest() {
        var parameterizedType = (ParameterizedType) TypeToken.of(getClass())
            .getSupertype(AbstractLombokConfigRuleTest.class)
            .getType();
        @SuppressWarnings("unchecked")
        var ruleClass = (Class<Rule>) TypeToken.of(parameterizedType.getActualTypeArguments()[0]).getRawType();
        this.rule = ruleClass.getConstructor().newInstance();

        this.rootDir = fs.getPath("/").resolve("repository-root");
        this.rootLombokConfig = rootDir.resolve(LOMBOK_CONFIG_FILE_NAME);
        createFile(createParentDirectories(this.rootLombokConfig));

        this.projectDir = rootDir.resolve("project");
        this.projectLombokConfig = projectDir.resolve(LOMBOK_CONFIG_FILE_NAME);
        createFile(createParentDirectories(this.projectLombokConfig));
    }


    @Test
    void documentationFileExists() {
        var pluginRootDirPath = System.getenv("LOMBOK_PLUGIN_ROOT_DIR");
        if (isEmpty(pluginRootDirPath)) {
            throw new IllegalStateException("Environment variable is not set or empty: LOMBOK_PLUGIN_ROOT_DIR");
        }

        var pluginRootDir = Paths.get(pluginRootDirPath).toAbsolutePath();
        var documentationFile = pluginRootDir.resolve(rule.getDocumentationFileRelativePath());
        assertThat(documentationFile)
            .isRegularFile();
    }


    @SneakyThrows
    protected void writeRootLombokConfig(String... lines) {
        writeString(
            createParentDirectories(rootLombokConfig),
            join("\n", lines),
            UTF_8
        );
    }

    @SneakyThrows
    protected void writeProjectLombokConfig(String... lines) {
        writeString(
            createParentDirectories(projectLombokConfig),
            join("\n", lines),
            UTF_8
        );
    }


    protected void assertThatRuleViolated() {
        assertThatViolatedRules()
            .contains(rule.getName());
    }

    protected void assertThatRuleNotViolated() {
        assertThatViolatedRules()
            .doesNotContain(rule.getName());
    }

    @CheckReturnValue
    protected CollectionAssert<String> assertThatViolatedRules() {
        validate();

        var violatedRules = reportedMessages.stream()
            .map(ReportedMessage::getRule)
            .collect(toUnmodifiableSet());
        return new CollectionAssert<>(violatedRules);
    }


    protected final List<ReportedMessage> reportedMessages = new ArrayList<>();

    protected void validate() {
        var config = new LombokConfig(projectLombokConfig);
        rule.validate(config, validationContext);
    }

    private final LombokConfigValidationContext validationContext = new LombokConfigValidationContext() {
        @Override
        public Path getRootPath() {
            return rootDir;
        }

        @Override
        public void report(String rule, Path path, @Nullable Integer lineNumber, String message) {
            reportedMessages.add(ReportedMessage.builder()
                .rule(rule)
                .path(path)
                .lineNumber(lineNumber)
                .message(message)
                .build()
            );
        }
    };

    @Value
    @Builder
    protected static class ReportedMessage {

        String rule;

        Path path;

        @Nullable
        Integer lineNumber;

        String message;

    }

}
