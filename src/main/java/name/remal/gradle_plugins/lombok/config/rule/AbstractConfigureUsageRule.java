package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;

import name.remal.gradle_plugins.lombok.config.LombokConfig;

abstract class AbstractConfigureUsageRule extends AbstractRule {

    protected abstract String getKey();


    @Override
    public final void validate(LombokConfig config, LombokConfigValidationContext context) {
        var key = getKey();
        var usageFlag = config.getUsageFlag(key);
        if (usageFlag == null) {
            context.report(getName(), config.getPath(), format(
                "Specify one of these values for `%s`: `error`, `warning`, `allow`."
                    + " See %s",
                key,
                getDocumentationUrl()
            ));
        }
    }

}
