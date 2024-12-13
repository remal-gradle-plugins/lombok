package name.remal.gradle_plugins.lombok;

import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.packageNameOf;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
import static name.remal.gradle_plugins.toolkit.testkit.ProjectValidations.executeAfterEvaluateActions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.TaskValidations;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class LombokPluginTest {

    final Project project;

    @Test
    void applyPlugin() {
        val pluginManager = project.getPluginManager();
        assertDoesNotThrow(() -> pluginManager.apply(LombokPlugin.class));
    }

    @Test
    void applyPluginWithJava() {
        val pluginManager = project.getPluginManager();
        assertDoesNotThrow(() -> pluginManager.apply(LombokPlugin.class));
        assertDoesNotThrow(() -> pluginManager.apply("java"));
    }

    @Test
    void pluginTasksDoNotHavePropertyProblems() {
        project.getPluginManager().apply(LombokPlugin.class);
        project.getPluginManager().apply("java");

        executeAfterEvaluateActions(project);

        val taskClassNamePrefix = packageNameOf(LombokPlugin.class) + '.';
        project.getTasks().stream()
            .filter(task -> {
                val taskClass = unwrapGeneratedSubclass(task.getClass());
                return taskClass.getName().startsWith(taskClassNamePrefix);
            })
            .forEach(TaskValidations::assertNoTaskPropertiesProblems);
    }

}
