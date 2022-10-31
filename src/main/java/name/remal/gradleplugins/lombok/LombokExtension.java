package name.remal.gradleplugins.lombok;

import static name.remal.gradleplugins.lombok.LombokDependencies.getLombokDependency;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.provider.Property;

@Getter
@Setter
public abstract class LombokExtension {

    public abstract Property<String> getLombokVersion();

    {
        getLombokVersion().convention(getLombokDependency("lombok").getVersion());
    }

}
