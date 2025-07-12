package name.remal.gradle_plugins.lombok.config;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;
import static name.remal.gradle_plugins.lombok.config.SystemProviders.getEnvVars;
import static name.remal.gradle_plugins.lombok.config.SystemProviders.getHomeDirPath;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
abstract class ImportInstructionResolver {

    @SuppressWarnings("java:S3776")
    public static ResolvedImport resolveImport(ImportInstruction instruction) {
        var value = instruction.getValue()
            .replace('\\', '/');

        if (value.isEmpty()) {
            return ResolvedImportError.builderFor(instruction)
                .message("Import path must not be empty")
                .build();
        }

        if (value.endsWith("!")) {
            return ResolvedImportError.builderFor(instruction)
                .message("Import path must not end with '!': " + value)
                .build();
        }

        var valueParts = Splitter.on('!').splitToList(value);
        if (valueParts.size() > 2) {
            return ResolvedImportError.builderFor(instruction)
                .message("Import path must not have multiple '!': " + value)
                .build();
        }

        var file = valueParts.get(0);
        if (file.startsWith("~")) {
            file = getHomeDirPath() + file.substring(1);
        }

        for (var envEntry : getEnvVars().entrySet()) {
            file = file.replace('<' + envEntry.getKey() + '>', envEntry.getValue());
        }


        var instructionFile = instruction.getFile();
        var fileSystemPath = instructionFile.getFileSystemPath();

        var filePath = fileSystemPath.getFileSystem().getPath(file);
        if (file.startsWith("/")) {
            filePath = normalizePath(filePath);
        }

        if (instructionFile instanceof LombokConfigPathArchive) {
            if (filePath.isAbsolute()) {
                return ResolvedImportError.builderFor(instruction)
                    .message("Config inside an archive must import configs only in that archive: " + value)
                    .build();
            }

            var instructionArchiveFile = (LombokConfigPathArchive) instructionFile;
            var entryName = instructionArchiveFile.getEntryName();
            var entryFilePath = getDirPathOf(Paths.get(entryName));
            var entryPath = entryFilePath.resolve(filePath).normalize();
            if (entryPath.toString().isEmpty()) {
                return ResolvedImportFile.builderFor(instruction)
                    .fileToImport(
                        new LombokConfigPathArchive(instructionArchiveFile.getArchivePath(), LOMBOK_CONFIG_FILE_NAME)
                    )
                    .build();

            } else if (entryPath.getName(0).toString().equals("..")) {
                return ResolvedImportError.builderFor(instruction)
                    .message("Config inside an archive must import configs only in that archive: " + value)
                    .build();

            } else {
                return ResolvedImportFile.builderFor(instruction)
                    .fileToImport(
                        new LombokConfigPathArchive(instructionArchiveFile.getArchivePath(), entryPath.toString())
                    )
                    .build();
            }
        }


        final LombokConfigPath fileToImport;
        var targetFilePath = normalizePath(getDirPathOf(fileSystemPath).resolve(filePath));
        if (file.endsWith(".zip")
            || file.endsWith(".jar")
        ) {
            String entryName;
            if (valueParts.size() == 1) {
                entryName = LOMBOK_CONFIG_FILE_NAME;
            } else {
                entryName = valueParts.get(1);
            }

            fileToImport = new LombokConfigPathArchive(targetFilePath, entryName);

        } else {
            fileToImport = new LombokConfigPathSystem(targetFilePath);
        }

        return ResolvedImportFile.builderFor(instruction)
            .fileToImport(fileToImport)
            .build();
    }


    @VisibleForTesting
    static Path getDirPathOf(Path path) {
        var parentPath = path.getParent();
        if (parentPath != null && !parentPath.equals(path)) {
            return parentPath;
        }

        if (path.isAbsolute()) {
            return path;
        }

        var pathString = path.toString();
        if (!pathString.isEmpty()) {
            var firstChar = pathString.charAt(0);
            if (firstChar == '/' || firstChar == '\\') {
                return Paths.get("/");
            }
        }

        return Paths.get("");
    }

}
