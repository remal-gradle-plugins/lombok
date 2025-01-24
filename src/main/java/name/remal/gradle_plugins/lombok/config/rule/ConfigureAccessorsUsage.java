package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;
import static name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants.getStringProperty;

import com.google.auto.service.AutoService;
import name.remal.gradle_plugins.lombok.config.LombokConfig;

@AutoService(LombokConfigRule.class)
public class ConfigureAccessorsUsage implements LombokConfigRule {

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        var accessorsUsage = config.getUsageFlag("lombok.accessors.flagUsage");
        if (accessorsUsage == null) {
            context.report(getName(), config.getPath(), format(
                "Specify one of these values for `lombok.accessors.flagUsage`: `error`, `warning`, `allow`."
                    + " See %s/blob/main/config-rules/ConfigureAccessorsUsage.md",
                getStringProperty("repository.html-url")
            ));
        }
    }

}
