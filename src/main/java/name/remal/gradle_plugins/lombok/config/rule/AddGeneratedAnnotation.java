package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;
import static name.remal.gradle_plugins.lombok.config.rule.DocUtils.PLUGIN_REPOSITORY_HTML_URL;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import lombok.val;
import name.remal.gradle_plugins.lombok.config.LombokConfig;

@AutoService(LombokConfigRule.class)
public class AddGeneratedAnnotation implements LombokConfigRule {

    @VisibleForTesting
    static final List<String> ADD_GENERATED_ANNOTATION_CONFIG_KEYS = ImmutableList.of(
        "lombok.addLombokGeneratedAnnotation",
        "lombok.addJakartaGeneratedAnnotation",
        "lombok.addJavaxGeneratedAnnotation",
        "lombok.addGeneratedAnnotation"
    );

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        val hasAddGeneratedAnnotationConfigKey = ADD_GENERATED_ANNOTATION_CONFIG_KEYS.stream()
            .map(config::getBoolean)
            .anyMatch(Objects::nonNull);
        if (hasAddGeneratedAnnotationConfigKey) {
            return;
        }

        context.report(getName(), config.getPath(), format(
            "Configure `lombok.addLombokGeneratedAnnotation` or `lombok.addJavaxGeneratedAnnotation`."
                + " See %s/blob/main/config-rules/AddGeneratedAnnotation.md",
            PLUGIN_REPOSITORY_HTML_URL
        ));
    }

}
