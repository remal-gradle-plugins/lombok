package name.remal.gradleplugins.lombok.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import lombok.SneakyThrows;

public interface LombokConfigPath {

    Path getFileSystemPath();

    @SneakyThrows
    default FileTime getLastModifiedTime() {
        return Files.getLastModifiedTime(getFileSystemPath());
    }

    String readContent();

    String toString();

}
