package name.remal.gradleplugins.lombok.config.rule;

import static java.lang.String.format;
import static name.remal.gradleplugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;
import static name.remal.gradleplugins.lombok.config.rule.DocUtils.PLUGIN_REPOSITORY_HTML_URL;
import static name.remal.gradleplugins.lombok.config.rule.Utils.getRootPathOf;

import com.google.auto.service.AutoService;
import lombok.val;
import name.remal.gradleplugins.lombok.config.LombokConfig;

@AutoService(LombokConfigRule.class)
public class StopBubblingAtRoot implements LombokConfigRule {

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        val rootPath = getRootPathOf(context);
        val rootLombokConfigPath = rootPath.resolve(LOMBOK_CONFIG_FILE_NAME);

        val rootConfigFile = config.getConfigFiles().stream()
            .filter(it -> rootLombokConfigPath.equals(it.getFile().getFileSystemPath()))
            .findFirst()
            .orElse(null);

        if (rootConfigFile == null || !rootConfigFile.isStopBubbling()) {
            context.report(rootPath, format(
                "Root project or repository root doesn't contain %s file."
                    + " Create such a file with `config.stopBubbling = true` line to make the build system independent."
                    + " See %s/blob/main/config-rules/StopBubblingAtRoot.md",
                LOMBOK_CONFIG_FILE_NAME,
                PLUGIN_REPOSITORY_HTML_URL
            ));
        }

    }

}
