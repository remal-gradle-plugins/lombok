package name.remal.gradle_plugins.lombok.config;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;

import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.meta.When;
import lombok.SneakyThrows;
import lombok.Value;

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
        var content = forEntry(stream ->
            stream != null
                ? new String(toByteArray(stream), UTF_8)
                : null
        );

        if (content == null) {
            throw new NoSuchFileException(toString());
        }

        return content;
    }

    @FunctionalInterface
    private interface EntryProcessor<T> {
        @Nonnull(when = When.UNKNOWN)
        T process(@Nullable InputStream inputStream) throws Throwable;
    }

    @SneakyThrows
    @Nonnull(when = When.UNKNOWN)
    @SuppressWarnings("java:S5042")
    private <T> T forEntry(EntryProcessor<T> action) {
        var entryNameToFind = getEntryName();
        try (var stream = newInputStream(getArchivePath())) {
            try (var zipStream = new ZipInputStream(stream, UTF_8)) {
                while (true) {
                    var entry = zipStream.getNextEntry();
                    if (entry == null) {
                        return action.process(null);
                    }

                    if (entry.getName().equals(entryNameToFind)) {
                        return action.process(zipStream);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "zip:" + getArchivePath().toUri() + "!/" + getEntryName();
    }

}
