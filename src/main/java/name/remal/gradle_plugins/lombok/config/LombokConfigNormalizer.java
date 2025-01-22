package name.remal.gradle_plugins.lombok.config;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.lombok.config.LombokConfigurationKeys.findLombokConfigurationKeyFor;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class LombokConfigNormalizer {

    public static String normalizeLombokConfigKey(String key) {
        var configurationKey = findLombokConfigurationKeyFor(key);
        return configurationKey != null ? configurationKey.getName() : key.toLowerCase();
    }

}
