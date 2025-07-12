package name.remal.gradle_plugins.lombok.config;

import static java.lang.Boolean.parseBoolean;
import static java.util.stream.Collectors.toUnmodifiableList;
import static name.remal.gradle_plugins.lombok.config.LombokConfigFileProperty.byLombokConfigKey;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.CLEAR;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.MINUS;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.PLUS;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.SET;
import static name.remal.gradle_plugins.lombok.config.LombokConfigUsageFlag.lombokConfigUsageFlagOf;

import com.google.common.collect.ImmutableList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.jetbrains.annotations.Unmodifiable;

interface WithProperties {

    Stream<LombokConfigFileProperty> streamProperties();

    @Unmodifiable
    default List<LombokConfigFileProperty> getProperties() {
        return streamProperties().collect(toUnmodifiableList());
    }


    @Nullable
    default String get(String key) {
        var property = streamProperties()
            .filter(byLombokConfigKey(key))
            .reduce((first, second) -> second)
            .orElse(null);

        if (property == null || property.getOperator() != SET) {
            return null;
        }

        return property.getValue();
    }

    default String get(String key, String defaultValue) {
        var value = get(key);
        return value != null ? value : defaultValue;
    }


    @Nullable
    default Boolean getBoolean(String key) {
        var value = get(key);
        return value != null ? parseBoolean(value) : null;
    }

    default boolean getBoolean(String key, boolean defaultValue) {
        var value = getBoolean(key);
        return value != null ? value : defaultValue;
    }


    @Nullable
    default LombokConfigUsageFlag getUsageFlag(String key) {
        var value = get(key);
        return value != null ? lombokConfigUsageFlagOf(value) : null;
    }

    default LombokConfigUsageFlag getUsageFlag(String key, LombokConfigUsageFlag defaultValue) {
        var value = getUsageFlag(key);
        return value != null ? value : defaultValue;
    }


    @Unmodifiable
    default List<String> getList(String key) {
        Set<String> result = new LinkedHashSet<>();
        streamProperties()
            .filter(byLombokConfigKey(key))
            .forEach(property -> {
                var operator = property.getOperator();
                if (operator == PLUS) {
                    result.add(property.getValue());
                } else if (operator == MINUS) {
                    result.remove(property.getValue());
                } else if (operator == CLEAR) {
                    result.clear();
                }
            });
        return ImmutableList.copyOf(result);
    }

}
