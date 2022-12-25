package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;
import static name.remal.gradle_plugins.lombok.config.rule.DocUtils.PLUGIN_REPOSITORY_HTML_URL;

import com.google.auto.service.AutoService;
import name.remal.gradle_plugins.lombok.config.LombokConfig;

@AutoService(LombokConfigRule.class)
public class AddGeneratedAnnotation implements LombokConfigRule {

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        if (config.getBoolean("lombok.addLombokGeneratedAnnotation") != null) {
            return;
        }
        if (config.getBoolean("lombok.addJavaxGeneratedAnnotation") != null) {
            return;
        }
        if (config.getBoolean("lombok.addGeneratedAnnotation") != null) {
            return;
        }

        context.report(getName(), config.getPath(), format(
            "Configure `lombok.addLombokGeneratedAnnotation` or `lombok.addJavaxGeneratedAnnotation`."
                + " See %s/blob/main/config-rules/AddGeneratedAnnotation.md",
            PLUGIN_REPOSITORY_HTML_URL
        ));
    }

}
