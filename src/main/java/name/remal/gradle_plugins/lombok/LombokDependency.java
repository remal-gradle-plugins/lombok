package name.remal.gradle_plugins.lombok;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class LombokDependency {
    LombokDependencyType type;
    String group;
    String name;
    String version;
}
