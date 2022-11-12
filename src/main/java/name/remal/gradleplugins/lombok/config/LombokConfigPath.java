package name.remal.gradleplugins.lombok.config;

import java.nio.file.Path;

public interface LombokConfigPath {

    Path getFileSystemPath();

    String readContent();

    String toString();

}
