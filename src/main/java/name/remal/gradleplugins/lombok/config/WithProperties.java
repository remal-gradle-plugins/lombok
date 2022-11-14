package name.remal.gradleplugins.lombok.config;

import static java.lang.Boolean.parseBoolean;
import static name.remal.gradleplugins.lombok.config.LombokConfigFileProperty.byLombokConfigKey;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.CLEAR;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.MINUS;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.PLUS;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.SET;
import static name.remal.gradleplugins.lombok.config.LombokConfigUsageFlag.lombokConfigUsageFlagOf;

import com.google.common.collect.ImmutableList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;

interface WithProperties {

    @Unmodifiable
    List<LombokConfigFileProperty> getProperties();


    @Nullable
    default String get(String key) {
        val property = getProperties().stream()
            .filter(byLombokConfigKey(key))
            .reduce((first, second) -> second)
            .orElse(null);

        if (property == null || property.getOperator() != SET) {
            return null;
        }

        return property.getValue();
    }

    default String get(String key, String defaultValue) {
        val value = get(key);
        return value != null ? value : defaultValue;
    }


    @Nullable
    default Boolean getBoolean(String key) {
        val value = get(key);
        return value != null ? parseBoolean(value) : null;
    }

    default boolean getBoolean(String key, boolean defaultValue) {
        val value = getBoolean(key);
        return value != null ? value : defaultValue;
    }


    @Nullable
    default LombokConfigUsageFlag getUsageFlag(String key) {
        val value = get(key);
        return value != null ? lombokConfigUsageFlagOf(value) : null;
    }

    default LombokConfigUsageFlag getUsageFlag(String key, LombokConfigUsageFlag defaultValue) {
        val value = getUsageFlag(key);
        return value != null ? value : defaultValue;
    }


    @Unmodifiable
    default List<String> getList(String key) {
        Set<String> result = new LinkedHashSet<>();
        getProperties().stream()
            .filter(byLombokConfigKey(key))
            .forEach(property -> {
                val operator = property.getOperator();
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
