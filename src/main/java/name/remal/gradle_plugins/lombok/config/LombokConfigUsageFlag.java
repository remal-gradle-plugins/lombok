package name.remal.gradle_plugins.lombok.config;

import javax.annotation.Nullable;
import org.jetbrains.annotations.Contract;

public enum LombokConfigUsageFlag {

    WARNING,
    ERROR,
    ALLOW,
    ;

    @Nullable
    @Contract("null->null")
    public static LombokConfigUsageFlag lombokConfigUsageFlagOf(@Nullable String value) {
        for (var enumValue : values()) {
            if (enumValue.name().equalsIgnoreCase(value)) {
                return enumValue;
            }
        }

        return null;
    }

}
