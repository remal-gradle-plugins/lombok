package name.remal.gradleplugins.lombok.config.rule;

import static java.lang.String.format;
import static name.remal.gradleplugins.lombok.config.rule.DocUtils.PLUGIN_REPOSITORY_HTML_URL;

import com.google.auto.service.AutoService;
import lombok.val;
import name.remal.gradleplugins.lombok.config.LombokConfig;

@AutoService(LombokConfigRule.class)
public class ConfigureAccessorsUsage implements LombokConfigRule {

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        val accessorsUsage = config.getUsageFlag("lombok.accessors.flagUsage");
        if (accessorsUsage == null) {
            context.report(getName(), config.getPath(), format(
                "Specify one of these values for `lombok.accessors.flagUsage`: `error`, `warning`, `allow`."
                    + " See %s/blob/main/config-rules/ConfigureAccessorsUsage.md",
                PLUGIN_REPOSITORY_HTML_URL
            ));
        }
    }

}
