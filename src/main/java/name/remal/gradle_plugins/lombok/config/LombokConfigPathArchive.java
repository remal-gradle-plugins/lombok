package name.remal.gradle_plugins.lombok.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;

import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;
import lombok.SneakyThrows;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
public class LombokConfigPathArchive implements LombokConfigPath {

    Path archivePath;
    String entryName;

    public LombokConfigPathArchive(Path archivePath, String entryName) {
        entryName = entryName.replace('\\', '/');
        while (entryName.startsWith("/")) {
            entryName = entryName.substring(1);
        }

        this.archivePath = archivePath;
        this.entryName = entryName;
    }

    @Override
    public Path getFileSystemPath() {
        return getArchivePath();
    }

    @Override
    @SneakyThrows
    public String readContent() {
        var content = forEntry(stream -> {
            if (stream == null) {
                return null;
            }

            var bytes = stream.readAllBytes();
            return new String(bytes, UTF_8);
        });

        if (content == null) {
            throw new NoSuchFileException(toString());
        }

        return content;
    }

    @Nullable
    @SneakyThrows
    @SuppressWarnings("java:S5042")
    private <T> T forEntry(EntryProcessor<T> action) {
        var entryNameToFind = getEntryName();
        try (var stream = newInputStream(getArchivePath())) {
            try (var zipStream = new ZipInputStream(stream, UTF_8)) {
                while (true) {
                    var entry = zipStream.getNextEntry();
                    if (entry == null || entry.isDirectory()) {
                        return action.process(null);
                    }

                    if (entry.getName().equals(entryNameToFind)) {
                        return action.process(zipStream);
                    }
                }
            }
        }
    }

    @FunctionalInterface
    private interface EntryProcessor<T> {
        @Nullable
        T process(@Nullable InputStream inputStream) throws Throwable;
    }

    @Override
    public String toString() {
        return "zip:" + getArchivePath().toUri() + "!/" + getEntryName();
    }

}
