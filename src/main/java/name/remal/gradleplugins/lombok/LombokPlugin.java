package name.remal.gradleplugins.lombok;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LombokPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {


        project.getPluginManager().withPlugin("java", __ -> configureJavaProject(project));
    }

    private static void configureJavaProject(Project project) {

    }

}
