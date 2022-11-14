package name.remal.gradleplugins.lombok.config;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.lombok.config.LombokConfigurationKeys.findLombokConfigurationKeyFor;

import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class LombokConfigNormalizer {

    public static String normalizeLombokConfigKey(String key) {
        val configurationKey = findLombokConfigurationKeyFor(key);
        return configurationKey != null ? configurationKey.getName() : key.toLowerCase();
    }

}
