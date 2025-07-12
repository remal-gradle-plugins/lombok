package name.remal.gradle_plugins.lombok.config.rule;

import static java.lang.String.format;
import static name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants.getStringProperty;

import com.google.errorprone.annotations.ForOverride;

abstract class AbstractRule implements LombokConfigRule {

    @ForOverride
    protected String getDocumentationFileNameWithoutExtension() {
        return getName();
    }


    protected final String getDocumentationFileRelativePath() {
        return format(
            "config-rules/%s.md",
            getDocumentationFileNameWithoutExtension()
        );
    }

    protected final String getDocumentationUrl() {
        return format(
            "%s/blob/main/%s",
            getStringProperty("repository.html-url"),
            getDocumentationFileRelativePath()
        );
    }

}
