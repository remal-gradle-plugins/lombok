package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;
import static java.lang.String.join;

import com.google.auto.service.AutoService;
import java.util.List;
import name.remal.gradle_plugins.lombok.config.LombokConfig;

@AutoService(LombokConfigRule.class)
public class DisableGlobalFieldDefaults extends AbstractRule {

    @Override
    public void validate(LombokConfig config, LombokConfigValidationContext context) {
        var keys = List.of(
            "lombok.fieldDefaults.defaultPrivate",
            "lombok.fieldDefaults.defaultFinal"
        );
        keys.forEach(key -> {
            var enabled = config.getBoolean(key, false);
            if (enabled) {
                context.report(getName(), config.getPath(), format(
                    "Disable `%s`."
                        + " See %s",
                    join("` and `", keys),
                    getDocumentationUrl()
                ));
            }
        });
    }
}
