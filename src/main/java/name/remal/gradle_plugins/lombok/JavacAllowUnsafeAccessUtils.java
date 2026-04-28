package name.remal.gradle_plugins.lombok;

import static java.lang.Integer.parseInt;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;
import org.gradle.api.JavaVersion;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
abstract class JavacAllowUnsafeAccessUtils {

    private static final int FIRST_SUPPORTED_JAVA_VERSION = 23;

    public static boolean shouldSuppressUnsafeWarningJvmArgsBeAdded(JavaVersion javaVersion) {
        var majorVersion = parseInt(javaVersion.getMajorVersion());
        return majorVersion >= FIRST_SUPPORTED_JAVA_VERSION;
    }


    private static final List<String> JAVAC_ALLOW_UNSAFE_ACCESS_JVM_ARGS = List.of(
        "--sun-misc-unsafe-memory-access=allow"
    );

    public static List<String> getJavacAllowUnsafeAccessJvmArgs() {
        return JAVAC_ALLOW_UNSAFE_ACCESS_JVM_ARGS;
    }

    public static List<String> withJavacAllowUnsafeAccess(@Nullable Collection<String> args) {
        List<String> result = new ArrayList<>();
        if (args != null) {
            result.addAll(args);
        }

        for (var jvmArg : getJavacAllowUnsafeAccessJvmArgs()) {
            if (!result.contains(jvmArg)) {
                result.add(jvmArg);
            }
        }

        return result;
    }

}
