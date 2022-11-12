package name.remal.gradleplugins.lombok.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;

import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

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
        val bytes = readAllBytes(getPath());
        return new String(bytes, UTF_8);
    }

    @Override
    public String toString() {
        return getPath().toString();
    }

}
