package name.remal.gradleplugins.lombok.config.rule;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.ProjectUtils.getTopLevelDirOf;
import static name.remal.gradleplugins.toolkit.git.GitUtils.findGitRepositoryRootFor;

import java.nio.file.Path;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.Project;

@NoArgsConstructor(access = PRIVATE)
abstract class Utils {

    public static Path getRootPathOf(LombokConfigValidationContext context) {
        return getRootPathOf(context.getProject());
    }

    public static Path getRootPathOf(Project project) {
        val topLevelDir = getTopLevelDirOf(project);
        val repositoryRoot = findGitRepositoryRootFor(topLevelDir);
        if (repositoryRoot != null) {
            return repositoryRoot;
        }

        return topLevelDir;
    }

}
