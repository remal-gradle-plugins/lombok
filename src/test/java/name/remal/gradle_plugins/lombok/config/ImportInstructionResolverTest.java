package name.remal.gradle_plugins.lombok.config;

import static java.util.Collections.singletonMap;
import static name.remal.gradle_plugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("DuplicateExpressions")
class ImportInstructionResolverTest {

    final ImportInstruction instruction = mock(ImportInstruction.class);

    @BeforeEach
    void beforeEach() {
        var currentFile = mock(LombokConfigPathSystem.class, CALLS_REAL_METHODS);
        lenient().when(currentFile.getPath()).thenReturn(normalizePath(Paths.get("/root/some/inner/dir/file")));

        lenient().when(instruction.getFile()).thenReturn(currentFile);
    }


    @Test
    void empty() {
        when(instruction.getValue()).thenReturn("");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportError.class);
    }

    @Test
    void endsWithArchiveDelim() {
        when(instruction.getValue()).thenReturn("file.zip!");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportError.class);
    }

    @Test
    void withMultipleArchiveDelim() {
        when(instruction.getValue()).thenReturn("file.zip!entry1!entry2");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportError.class);
    }

    @Test
    void userHomeSubstitution() {
        var currentFile = mock(LombokConfigPathSystem.class, CALLS_REAL_METHODS);
        when(instruction.getFile()).thenReturn(currentFile);
        when(currentFile.getPath()).thenReturn(normalizePath(Paths.get("/root/dir/file")));

        try (var systemProviders = mockStatic(SystemProviders.class)) {
            systemProviders.when(SystemProviders::getHomeDirPath).thenReturn("/home");

            when(instruction.getValue()).thenReturn("~/file");

            assertThat(ImportInstructionResolver.resolveImport(instruction))
                .isInstanceOf(ResolvedImportFile.class)
                .extracting(ResolvedImportFile.class::cast)
                .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
                .isInstanceOf(LombokConfigPathSystem.class)
                .extracting(LombokConfigPathSystem.class::cast)
                .extracting(LombokConfigPathSystem::getPath).as("path")
                .isEqualTo(normalizePath(Paths.get("/home/file")))
            ;
        }
    }

    @Test
    void envVarSubstitution() {
        var currentFile = mock(LombokConfigPathSystem.class, CALLS_REAL_METHODS);
        when(instruction.getFile()).thenReturn(currentFile);
        when(currentFile.getPath()).thenReturn(normalizePath(Paths.get("/root/dir/file")));

        try (var systemProviders = mockStatic(SystemProviders.class)) {
            var varName = "__TEST_VAR__" + System.nanoTime();
            systemProviders.when(SystemProviders::getEnvVars).thenReturn(singletonMap(varName, "dir"));

            when(instruction.getValue()).thenReturn("/root/<" + varName + ">/file");

            assertThat(ImportInstructionResolver.resolveImport(instruction))
                .isInstanceOf(ResolvedImportFile.class)
                .extracting(ResolvedImportFile.class::cast)
                .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
                .isInstanceOf(LombokConfigPathSystem.class)
                .extracting(LombokConfigPathSystem.class::cast)
                .extracting(LombokConfigPathSystem::getPath).as("path")
                .isEqualTo(normalizePath(Paths.get("/root/dir/file")))
            ;
        }
    }

    @Test
    void absoluteInArchive() {
        var archiveFile = mock(LombokConfigPathArchive.class, CALLS_REAL_METHODS);
        when(instruction.getFile()).thenReturn(archiveFile);
        when(archiveFile.getArchivePath()).thenReturn(normalizePath(Paths.get("/root/archive.zip")));
        when(archiveFile.getEntryName()).thenReturn("entry");

        when(instruction.getValue()).thenReturn("/root/file");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportError.class);
    }

    @Test
    void outOfArchive() {
        var archiveFile = mock(LombokConfigPathArchive.class, CALLS_REAL_METHODS);
        when(instruction.getFile()).thenReturn(archiveFile);
        when(archiveFile.getArchivePath()).thenReturn(normalizePath(Paths.get("/root/archive.zip")));
        when(archiveFile.getEntryName()).thenReturn("entry");

        when(instruction.getValue()).thenReturn("../../file");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportError.class);
    }

    @Test
    void rootOfArchive() {
        var archiveFile = mock(LombokConfigPathArchive.class, CALLS_REAL_METHODS);
        when(instruction.getFile()).thenReturn(archiveFile);
        when(archiveFile.getArchivePath()).thenReturn(normalizePath(Paths.get("/root/archive.zip")));
        when(archiveFile.getEntryName()).thenReturn("entry");

        when(instruction.getValue()).thenReturn("./");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportFile.class)
            .extracting(ResolvedImportFile.class::cast)
            .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
            .isInstanceOf(LombokConfigPathArchive.class)
            .extracting(LombokConfigPathArchive.class::cast)
            .satisfies(file -> {
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getArchivePath).as("archivePath")
                    .isEqualTo(archiveFile.getArchivePath());
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getEntryName).as("entryName")
                    .isEqualTo(LOMBOK_CONFIG_FILE_NAME);
            })
        ;
    }

    @Test
    void relativeInArchive() {
        var archiveFile = mock(LombokConfigPathArchive.class, CALLS_REAL_METHODS);
        when(instruction.getFile()).thenReturn(archiveFile);
        when(archiveFile.getArchivePath()).thenReturn(normalizePath(Paths.get("/root/archive.zip")));
        when(archiveFile.getEntryName()).thenReturn("root/entry");

        when(instruction.getValue()).thenReturn("../file");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportFile.class)
            .extracting(ResolvedImportFile.class::cast)
            .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
            .isInstanceOf(LombokConfigPathArchive.class)
            .extracting(LombokConfigPathArchive.class::cast)
            .satisfies(file -> {
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getArchivePath).as("archivePath")
                    .isEqualTo(archiveFile.getArchivePath());
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getEntryName).as("entryName")
                    .isEqualTo("file");
            })
        ;
    }

    @Test
    void absoluteArchive() {
        when(instruction.getValue()).thenReturn("/file.zip");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportFile.class)
            .extracting(ResolvedImportFile.class::cast)
            .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
            .isInstanceOf(LombokConfigPathArchive.class)
            .extracting(LombokConfigPathArchive.class::cast)
            .satisfies(file -> {
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getArchivePath).as("archivePath")
                    .isEqualTo(normalizePath(Paths.get("/file.zip")));
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getEntryName).as("entryName")
                    .isEqualTo(LOMBOK_CONFIG_FILE_NAME);
            })
        ;
    }

    @Test
    void absoluteArchiveWithEntry() {
        when(instruction.getValue()).thenReturn("/file.zip!/entry");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportFile.class)
            .extracting(ResolvedImportFile.class::cast)
            .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
            .isInstanceOf(LombokConfigPathArchive.class)
            .extracting(LombokConfigPathArchive.class::cast)
            .satisfies(file -> {
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getArchivePath).as("archivePath")
                    .isEqualTo(normalizePath(Paths.get("/file.zip")));
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getEntryName).as("entryName")
                    .isEqualTo("entry");
            })
        ;
    }

    @Test
    void absolute() {
        when(instruction.getValue()).thenReturn("/file");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportFile.class)
            .extracting(ResolvedImportFile.class::cast)
            .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
            .isInstanceOf(LombokConfigPathSystem.class)
            .extracting(LombokConfigPathSystem.class::cast)
            .extracting(LombokConfigPathSystem::getPath).as("path")
            .isEqualTo(normalizePath(Paths.get("/file")))
        ;
    }

    @Test
    void relativeArchive() {
        var currentFile = mock(LombokConfigPathSystem.class, CALLS_REAL_METHODS);
        when(instruction.getFile()).thenReturn(currentFile);
        when(currentFile.getPath()).thenReturn(normalizePath(Paths.get("/root/dir/file")));

        when(instruction.getValue()).thenReturn("../other-dir/file.zip");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportFile.class)
            .extracting(ResolvedImportFile.class::cast)
            .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
            .isInstanceOf(LombokConfigPathArchive.class)
            .extracting(LombokConfigPathArchive.class::cast)
            .satisfies(file -> {
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getArchivePath).as("archivePath")
                    .isEqualTo(normalizePath(Paths.get("/root/other-dir/file.zip")));
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getEntryName).as("entryName")
                    .isEqualTo(LOMBOK_CONFIG_FILE_NAME);
            })
        ;
    }

    @Test
    void relativeArchiveWithEntry() {
        var currentFile = mock(LombokConfigPathSystem.class, CALLS_REAL_METHODS);
        when(instruction.getFile()).thenReturn(currentFile);
        when(currentFile.getPath()).thenReturn(normalizePath(Paths.get("/root/dir/file")));

        when(instruction.getValue()).thenReturn("../other-dir/file.zip!/entry");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportFile.class)
            .extracting(ResolvedImportFile.class::cast)
            .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
            .isInstanceOf(LombokConfigPathArchive.class)
            .extracting(LombokConfigPathArchive.class::cast)
            .satisfies(file -> {
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getArchivePath).as("archivePath")
                    .isEqualTo(normalizePath(Paths.get("/root/other-dir/file.zip")));
                assertThat(file)
                    .extracting(LombokConfigPathArchive::getEntryName).as("entryName")
                    .isEqualTo("entry");
            })
        ;
    }

    @Test
    void relative() {
        var currentFile = mock(LombokConfigPathSystem.class, CALLS_REAL_METHODS);
        when(instruction.getFile()).thenReturn(currentFile);
        when(currentFile.getPath()).thenReturn(normalizePath(Paths.get("/root/dir/file")));

        when(instruction.getValue()).thenReturn("../other-dir/file");

        assertThat(ImportInstructionResolver.resolveImport(instruction))
            .isInstanceOf(ResolvedImportFile.class)
            .extracting(ResolvedImportFile.class::cast)
            .extracting(ResolvedImportFile::getFileToImport).as("fileToImport")
            .isInstanceOf(LombokConfigPathSystem.class)
            .extracting(LombokConfigPathSystem.class::cast)
            .extracting(LombokConfigPathSystem::getPath).as("path")
            .isEqualTo(normalizePath(Paths.get("/root/other-dir/file")))
        ;
    }


    @Test
    void getDirPathOf() {
        assertThat(ImportInstructionResolver.getDirPathOf(Paths.get("")))
            .isEqualTo(Paths.get(""));

        assertThat(ImportInstructionResolver.getDirPathOf(Paths.get("/")))
            .isEqualTo(Paths.get("/"));

        assertThat(ImportInstructionResolver.getDirPathOf(Paths.get("/file")))
            .isEqualTo(Paths.get("/"));

        assertThat(ImportInstructionResolver.getDirPathOf(Paths.get("/dir/file")))
            .isEqualTo(Paths.get("/dir"));

        assertThat(ImportInstructionResolver.getDirPathOf(normalizePath(Paths.get("/"))))
            .isEqualTo(normalizePath(Paths.get("/")));

        assertThat(ImportInstructionResolver.getDirPathOf(normalizePath(Paths.get("/file"))))
            .isEqualTo(normalizePath(Paths.get("/")));

        assertThat(ImportInstructionResolver.getDirPathOf(normalizePath(Paths.get("/dir/file"))))
            .isEqualTo(normalizePath(Paths.get("/dir")));
    }

}
