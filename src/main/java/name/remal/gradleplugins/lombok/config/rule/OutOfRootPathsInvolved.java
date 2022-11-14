package name.remal.gradleplugins.lombok.config.rule;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static name.remal.gradleplugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;
import static name.remal.gradleplugins.lombok.config.rule.DocUtils.PLUGIN_REPOSITORY_HTML_URL;
import static name.remal.gradleplugins.lombok.config.rule.Utils.getRootPathOf;
import static name.remal.gradleplugins.toolkit.PredicateUtils.not;

import com.google.auto.service.AutoService;
import lombok.val;
import name.remal.gradleplugins.lombok.config.LombokConfig;
import name.remal.gradleplugins.lombok.config.LombokConfigFile;

@AutoService(LombokConfigRule.class)
public class OutOfRootPathsInvolved implements LombokConfigRule {

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        val rootPath = getRootPathOf(context);

        val outsideRootConfigs = config.getConfigFiles().stream()
            .filter(not(it -> it.getFile().getFileSystemPath().startsWith(rootPath)))
            .collect(toList());
        if (!outsideRootConfigs.isEmpty()) {
            context.report(rootPath, format(
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
