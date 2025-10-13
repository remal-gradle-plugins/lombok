package name.remal.gradle_plugins.lombok.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LombokConfigurationKey {

    String name;

    String description;

    boolean deprecated;

}
