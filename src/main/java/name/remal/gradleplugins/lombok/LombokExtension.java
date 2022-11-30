package name.remal.gradleplugins.lombok;

import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import name.remal.gradleplugins.lombok.config.LombokExtensionConfig;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

@Getter
@Setter
public abstract class LombokExtension {

    public abstract Property<String> getLombokVersion();


    private final LombokExtensionConfig config = getObjectFactory().newInstance(LombokExtensionConfig.class);

    public void config(Action<LombokExtensionConfig> action) {
        action.execute(config);
    }


    private final LombokExtensionDelombok delombok = getObjectFactory().newInstance(LombokExtensionDelombok.class);

    public void delombok(Action<LombokExtensionDelombok> action) {
        action.execute(delombok);
    }


    public abstract Property<Boolean> getFixJavacReflectionsAccess();

    public abstract Property<Boolean> getFixAnnotationProcessorsOrder();

    {
        getFixJavacReflectionsAccess().convention(true);
        getFixAnnotationProcessorsOrder().convention(true);
    }


    @Inject
    protected abstract ObjectFactory getObjectFactory();

}
