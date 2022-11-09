package name.remal.gradleplugins.lombok;

import static java.nio.file.Files.exists;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradleplugins.toolkit.PathUtils;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;

@NoArgsConstructor(access = PRIVATE)
abstract class LombokConfigUtils {

    private static final String LOMBOK_CONFIG_FILE_NAME = "lombok.config";

    public static FileCollection getAllLombokConfigFilesFor(Project project, SourceDirectorySet sourceDirectorySet) {
        return project.files(project.provider(() -> {
            Set<Path> paths = new LinkedHashSet<>();
            for (val srcDir : sourceDirectorySet.getSrcDirs()) {
                project.fileTree(srcDir, it -> it.include("**/" + LOMBOK_CONFIG_FILE_NAME))
                    .getFiles()
                    .stream()
                    .map(File::toPath)
                    .map(PathUtils::normalizePath)
                    .forEach(paths::add);

                Path currentDirPath = normalizePath(srcDir.toPath());
                while (currentDirPath != null) {
                    val configPath = currentDirPath.resolve(LOMBOK_CONFIG_FILE_NAME);
                    if (exists(configPath)) {
                        paths.add(configPath);
                    }
                    currentDirPath = currentDirPath.getParent();
                }
            }
            return paths;
        })).filter(File::exists);
    }

    /*
    private static Path getRootPathOf(Project project) {
        val topLevelDir = getTopLevelDirOf(project);
        val repositoryRoot = findGitRepositoryRootFor(topLevelDir);
        if (repositoryRoot != null) {
            return repositoryRoot;
        }

        return topLevelDir;
    }
    */

}
