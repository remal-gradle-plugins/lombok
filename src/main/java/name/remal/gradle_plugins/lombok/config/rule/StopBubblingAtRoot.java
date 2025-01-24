package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;
import static name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants.getStringProperty;
import static name.remal.gradle_plugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;

import com.google.auto.service.AutoService;
import name.remal.gradle_plugins.lombok.config.LombokConfig;

@AutoService(LombokConfigRule.class)
public class StopBubblingAtRoot implements LombokConfigRule {

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        var rootPath = context.getRootPath();
        var rootLombokConfigPath = rootPath.resolve(LOMBOK_CONFIG_FILE_NAME);

        var rootConfigFile = config.getConfigFiles().stream()
            .filter(it -> rootLombokConfigPath.equals(it.getFile().getFileSystemPath()))
            .findFirst()
            .orElse(null);

        if (rootConfigFile == null || !rootConfigFile.isStopBubbling()) {
            context.report(getName(), rootPath, format(
                "Root project or repository root doesn't contain %s file."
                    + " Create such a file with `config.stopBubbling = true` line to make the build system independent."
                    + " See %s/blob/main/config-rules/StopBubblingAtRoot.md",
                LOMBOK_CONFIG_FILE_NAME,
                getStringProperty("repository.html-url")
            ));
        }

    }

}
