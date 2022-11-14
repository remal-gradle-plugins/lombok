package name.remal.gradleplugins.lombok.config.rule;

import static java.util.Collections.emptySet;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import java.util.Set;
import name.remal.gradleplugins.lombok.config.LombokConfig;

public interface LombokConfigRule {

    default String getName() {
        return unwrapGeneratedSubclass(this.getClass()).getSimpleName();
    }

    default Set<String> getAliases() {
        return emptySet();
    }

    void validate(LombokConfig config, LombokConfigValidationContext context);

}
