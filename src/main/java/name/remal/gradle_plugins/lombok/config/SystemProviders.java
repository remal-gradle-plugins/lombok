package name.remal.gradle_plugins.lombok.config;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;

import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
abstract class SystemProviders {

    public static String getHomeDirPath() {
        val value = System.getProperty("user.home");
        if (isEmpty(value)) {
            throw new IllegalStateException("Empty system property: user.home");
        }
        return value;
    }

    public static Map<String, String> getEnvVars() {
        return System.getenv();
    }

}
