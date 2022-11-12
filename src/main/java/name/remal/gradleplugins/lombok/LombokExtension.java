package name.remal.gradleplugins.lombok;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.provider.Property;

@Getter
@Setter
public abstract class LombokExtension {

    public abstract Property<String> getLombokVersion();


    public abstract Property<Boolean> getOpenJavacPackages();

    public abstract Property<Boolean> getFixAnnotationProcessorsOrder();

    {
        getOpenJavacPackages().convention(true);
        getFixAnnotationProcessorsOrder().convention(true);
    }

}
