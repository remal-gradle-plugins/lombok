package name.remal.gradleplugins.lombok;

import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

@Getter
@Setter
public abstract class LombokExtensionDelombok {

    private final DelombokFormat format = getObjectFactory().newInstance(DelombokFormat.class);

    public void format(Action<DelombokFormat> action) {
        action.execute(format);
    }


    @Inject
    protected abstract ObjectFactory getObjectFactory();

}
