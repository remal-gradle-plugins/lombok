package name.remal.gradleplugins.lombok.config.rule;

import static java.lang.String.format;
import static name.remal.gradleplugins.lombok.config.rule.DocUtils.PLUGIN_REPOSITORY_HTML_URL;

import com.google.auto.service.AutoService;
import lombok.val;
import name.remal.gradleplugins.lombok.config.LombokConfig;

@AutoService(LombokConfigRule.class)
public class ConfigureUtilityClassUsage implements LombokConfigRule {

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        val utilityClassUsage = config.getUsageFlag("lombok.utilityClass.flagUsage");
        if (utilityClassUsage == null) {
            context.report(config.getPath(), format(
                "Specify one of these values for `lombok.utilityClass.flagUsage`: `error`, `warning`, `allow`."
                    + " See %s/blob/main/config-rules/ConfigureUtilityClassUsage.md",
                PLUGIN_REPOSITORY_HTML_URL
            ));
        }
    }

}
