package name.remal.gradle_plugins.lombok.config;

import java.util.function.Predicate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LombokConfigFileProperty implements WithFileLine {

    LombokConfigPath file;

    int lineNumber;

    String key;

    LombokConfigPropertyOperator operator;

    String value;


    public boolean is(String keyToCheck) {
        return getKey().equalsIgnoreCase(keyToCheck);
    }

    public static Predicate<LombokConfigFileProperty> byLombokConfigKey(String keyToCheck) {
        return property -> property.is(keyToCheck);
    }

}
