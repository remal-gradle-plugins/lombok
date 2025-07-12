package name.remal.gradle_plugins.lombok.config.rule;

import com.google.auto.service.AutoService;

@AutoService(LombokConfigRule.class)
public class ConfigureVarUsage extends AbstractConfigureUsageRule {

    @Override
    protected String getKey() {
        return "lombok.var.flagUsage";
    }

    @Override
    protected String getDocumentationFileNameWithoutExtension() {
        return "ConfigureValVarUsage";
    }

}
