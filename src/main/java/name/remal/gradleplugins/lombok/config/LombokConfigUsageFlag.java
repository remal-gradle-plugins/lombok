package name.remal.gradleplugins.lombok.config;

import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.Contract;

public enum LombokConfigUsageFlag {

    WARNING,
    ERROR,
    ALLOW,
    ;

    @Nullable
    @Contract("null->null")
    public static LombokConfigUsageFlag lombokConfigUsageFlagOf(@Nullable String value) {
        for (val enumValue : values()) {
            if (enumValue.name().equalsIgnoreCase(value)) {
                return enumValue;
            }
        }

        return null;
    }

}
