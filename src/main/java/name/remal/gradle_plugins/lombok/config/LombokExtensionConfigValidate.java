package name.remal.gradle_plugins.lombok.config;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.provider.SetProperty;

@Getter
@Setter
public abstract class LombokExtensionConfigValidate {

    public abstract SetProperty<String> getDisabledRules();

}
