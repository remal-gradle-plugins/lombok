package name.remal.gradle_plugins.lombok.config;

import static java.nio.file.Files.readString;

import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.Value;

@Value
public class LombokConfigPathSystem implements LombokConfigPath {

    Path path;

    @Override
    public Path getFileSystemPath() {
        return getPath();
    }

    @Override
    @SneakyThrows
    public String readContent() {
        return readString(getPath());
    }

    @Override
    public String toString() {
        return getPath().toString();
    }

}
