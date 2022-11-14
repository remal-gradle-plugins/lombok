package name.remal.gradleplugins.lombok.config;

import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

@Getter
@Setter
public abstract class LombokExtensionConfig {

    private final LombokExtensionConfigValidate validate = getObjectFactory()
        .newInstance(LombokExtensionConfigValidate.class);

    public void validate(Action<LombokExtensionConfigValidate> action) {
        action.execute(validate);
    }


    private final LombokExtensionConfigGenerate generate = getObjectFactory()
        .newInstance(LombokExtensionConfigGenerate.class);

    public void generate(Action<LombokExtensionConfigGenerate> action) {
        action.execute(generate);
    }


    @Inject
    protected abstract ObjectFactory getObjectFactory();

}
