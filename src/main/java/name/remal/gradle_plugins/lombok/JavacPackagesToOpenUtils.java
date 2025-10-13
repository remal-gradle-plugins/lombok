package name.remal.gradle_plugins.lombok;

import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;
import org.gradle.api.JavaVersion;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class JavacPackagesToOpenUtils {

    private static final List<String> JAVAC_PACKAGES_TO_OPEN = List.of(
    /*
    "jdk.compiler/com.sun.tools.javac.code",
    "jdk.compiler/com.sun.tools.javac.comp",
    "jdk.compiler/com.sun.tools.javac.file",
    "jdk.compiler/com.sun.tools.javac.main",
    "jdk.compiler/com.sun.tools.javac.model",
    "jdk.compiler/com.sun.tools.javac.parser",
    "jdk.compiler/com.sun.tools.javac.processing",
    "jdk.compiler/com.sun.tools.javac.tree",
    "jdk.compiler/com.sun.tools.javac.util"
    */
    );

    private static final List<String> JAVAC_PACKAGE_OPEN_JVM_ARGS = JAVAC_PACKAGES_TO_OPEN.stream()
        .map(it -> format("--add-opens=%s=ALL-UNNAMED", it))
        .collect(toUnmodifiableList());

    public static boolean shouldJavacPackageOpenJvmArgsBeAdded(JavaVersion javaVersion) {
        return javaVersion.isJava9Compatible();
    }

    public static List<String> getJavacPackageOpenJvmArgs() {
        return JAVAC_PACKAGE_OPEN_JVM_ARGS;
    }

    public static List<String> withJavacPackageOpens(@Nullable Collection<String> args) {
        List<String> result = new ArrayList<>();
        if (args != null) {
            result.addAll(args);
        }

        for (var jvmArg : getJavacPackageOpenJvmArgs()) {
            if (!result.contains(jvmArg)) {
                result.add(jvmArg);
            }
        }

        return result;
    }

}
