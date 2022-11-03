package name.remal.gradleplugins.lombok;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.JavaVersion;

@NoArgsConstructor(access = PRIVATE)
public abstract class JavacPackagesToOpenUtils {

    private static final List<String> JAVAC_PACKAGES_TO_OPEN = ImmutableList.of(
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

    private static final List<String> JAVAC_PACKAGE_OPEN_JVM_ARGS = ImmutableList.copyOf(JAVAC_PACKAGES_TO_OPEN.stream()
        .map(it -> format("--add-opens=%s=ALL-UNNAMED", it))
        .collect(toList())
    );

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

        for (val jvmArg : getJavacPackageOpenJvmArgs()) {
            if (!result.contains(jvmArg)) {
                result.add(jvmArg);
            }
        }

        return result;
    }

}
