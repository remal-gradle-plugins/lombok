package name.remal.gradle_plugins.lombok.config.rule;

import com.google.auto.service.AutoService;

@AutoService(LombokConfigRule.class)
public class ConfigureAccessorsUsage extends AbstractConfigureUsageRule {

    @Override
    protected String getKey() {
        return "lombok.accessors.flagUsage";
    }

}
