package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;

import java.nio.file.Path;
import name.remal.gradle_plugins.lombok.config.LombokConfigPath;
import name.remal.gradle_plugins.lombok.config.LombokConfigPathArchive;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;

public interface LombokConfigValidationContext {

    Path getRootPath();

    void report(
        String rule,
        Path path,
        @Nullable Integer lineNumber,
        @Language("TEXT") String message
    );

    default void report(
        String rule,
        Path path,
        @Language("TEXT") String message
    ) {
        report(rule, path, null, message);
    }

    default void report(
        String rule,
        LombokConfigPath path,
        @Nullable Integer lineNumber,
        @Language("TEXT") String message
    ) {
        if (path instanceof LombokConfigPathArchive) {
            message = format(
                "Archive entry %s: %s",
                ((LombokConfigPathArchive) path).getEntryName(),
                message
            );
        }

        report(rule, path.getFileSystemPath(), lineNumber, message);
    }

    default void report(
        String rule,
        LombokConfigPath path,
        @Language("TEXT") String message
    ) {
        report(rule, path, null, message);
    }

}
