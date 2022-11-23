package name.remal.gradleplugins.lombok.config.rule;

import static java.lang.String.format;

import java.nio.file.Path;
import javax.annotation.Nullable;
import name.remal.gradleplugins.lombok.config.LombokConfigPath;
import name.remal.gradleplugins.lombok.config.LombokConfigPathArchive;
import org.gradle.api.Project;
import org.intellij.lang.annotations.Language;

public interface LombokConfigValidationContext {

    Project getProject();

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
