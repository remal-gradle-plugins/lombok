package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;
import static name.remal.gradle_plugins.lombok.config.rule.DocUtils.PLUGIN_REPOSITORY_HTML_URL;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;

import com.google.auto.service.AutoService;
import lombok.val;
import name.remal.gradle_plugins.lombok.config.LombokConfig;
import name.remal.gradle_plugins.lombok.config.LombokConfigFile;

@AutoService(LombokConfigRule.class)
public class OutOfRootPathsInvolved implements LombokConfigRule {

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        val rootPath = context.getRootPath();

        val outsideRootConfigs = config.getConfigFiles().stream()
            .filter(not(it -> it.getFile().getFileSystemPath().startsWith(rootPath)))
            .collect(toList());
        if (!outsideRootConfigs.isEmpty()) {
            context.report(getName(), rootPath, format(
                "`%s` files outside of the root project or the repository root are involved into the build."
                    + " See %s/blob/main/config-rules/OutOfRootPathsInvolved.md"
                    + "\n* %s",
                LOMBOK_CONFIG_FILE_NAME,
                PLUGIN_REPOSITORY_HTML_URL,
                outsideRootConfigs.stream()
                    .map(LombokConfigFile::getSource)
                    .collect(joining("\n* "))
            ));
        }
    }
}
