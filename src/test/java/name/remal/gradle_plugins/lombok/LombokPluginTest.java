package name.remal.gradle_plugins.lombok;

import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.packageNameOf;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
import static name.remal.gradle_plugins.toolkit.testkit.ProjectValidations.executeAfterEvaluateActions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.TaskValidations;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class LombokPluginTest {

    final Project project;

    @Test
    void applyPlugin() {
        var pluginManager = project.getPluginManager();
        assertDoesNotThrow(() -> pluginManager.apply(LombokPlugin.class));
    }

    @Test
    void applyPluginWithJava() {
        var pluginManager = project.getPluginManager();
        assertDoesNotThrow(() -> pluginManager.apply(LombokPlugin.class));
        assertDoesNotThrow(() -> pluginManager.apply("java"));
    }

    @Test
    void pluginTasksDoNotHavePropertyProblems() {
        project.getPluginManager().apply(LombokPlugin.class);
        project.getPluginManager().apply("java");

        executeAfterEvaluateActions(project);

        var taskClassNamePrefix = packageNameOf(LombokPlugin.class) + '.';
        project.getTasks().stream()
            .filter(task -> {
                var taskClass = unwrapGeneratedSubclass(task.getClass());
                return taskClass.getName().startsWith(taskClassNamePrefix);
            })
            .map(TaskValidations::markTaskDependenciesAsSkipped)
            .forEach(TaskValidations::assertNoTaskPropertiesProblems);
    }

}
