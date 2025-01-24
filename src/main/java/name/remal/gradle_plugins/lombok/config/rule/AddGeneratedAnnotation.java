package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;
import static name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants.getStringProperty;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Objects;
import name.remal.gradle_plugins.lombok.config.LombokConfig;

@AutoService(LombokConfigRule.class)
public class AddGeneratedAnnotation implements LombokConfigRule {

    @VisibleForTesting
    static final List<String> ADD_GENERATED_ANNOTATION_CONFIG_KEYS = List.of(
        "lombok.addLombokGeneratedAnnotation",
        "lombok.addJakartaGeneratedAnnotation",
        "lombok.addJavaxGeneratedAnnotation",
        "lombok.addGeneratedAnnotation"
    );

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        var hasAddGeneratedAnnotationConfigKey = ADD_GENERATED_ANNOTATION_CONFIG_KEYS.stream()
            .map(config::getBoolean)
            .anyMatch(Objects::nonNull);
        if (hasAddGeneratedAnnotationConfigKey) {
            return;
        }

        context.report(getName(), config.getPath(), format(
            "Configure `lombok.addLombokGeneratedAnnotation` or `lombok.addJavaxGeneratedAnnotation`."
                + " See %s/blob/main/config-rules/AddGeneratedAnnotation.md",
            getStringProperty("repository.html-url")
        ));
    }

}
