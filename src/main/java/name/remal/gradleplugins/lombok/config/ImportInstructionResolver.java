package name.remal.gradleplugins.lombok.config;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;
import static name.remal.gradleplugins.lombok.config.SystemProviders.getEnvVars;
import static name.remal.gradleplugins.lombok.config.SystemProviders.getHomeDirPath;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
abstract class ImportInstructionResolver {

    @SuppressWarnings("java:S3776")
    public static ResolvedImport resolveImport(ImportInstruction instruction) {
        String value = instruction.getValue();

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

        val valueParts = Splitter.on('!').splitToList(value);
        if (valueParts.size() > 2) {
            return ResolvedImportError.builderFor(instruction)
                .message("Import path must not have multiple '!': " + value)
                .build();
        }

        String file = valueParts.get(0);
        if (file.startsWith("~")) {
            file = getHomeDirPath() + file.substring(1);
        }

        for (val envEntry : getEnvVars().entrySet()) {
            file = file.replace('<' + envEntry.getKey() + '>', envEntry.getValue());
        }


        val instructionFile = instruction.getFile();
        val fileSystemPath = instructionFile.getFileSystemPath();

        Path filePath = fileSystemPath.getFileSystem().getPath(file);
        if (file.startsWith("/") || file.startsWith("\\")) {
            filePath = normalizePath(filePath);
        }

        if (instructionFile instanceof LombokConfigPathArchive) {
            if (filePath.isAbsolute()) {
                return ResolvedImportError.builderFor(instruction)
                    .message("Config inside an archive must import configs only in that archive: " + value)
                    .build();
            }

            val instructionArchiveFile = (LombokConfigPathArchive) instructionFile;
            val entryName = instructionArchiveFile.getEntryName();
            val entryFilePath = getDirPathOf(Paths.get(entryName));
            val entryPath = entryFilePath.resolve(filePath).normalize();
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
        val targetFilePath = normalizePath(getDirPathOf(fileSystemPath).resolve(filePath));
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
        val parentPath = path.getParent();
        if (parentPath != null && !parentPath.equals(path)) {
            return parentPath;
        }

        val pathString = path.toString();
        if (!pathString.isEmpty()) {
            val firstChar = pathString.charAt(0);
            if (firstChar == '/') {
                return Paths.get("/");
            } else if (firstChar == '\\') {
                return Paths.get("\\");
            } else if (path.isAbsolute()) {
                return path;
            }
        }

        return Paths.get("");
    }

}
