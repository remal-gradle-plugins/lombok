package name.remal.gradle_plugins.lombok;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import lombok.RequiredArgsConstructor;
import lombok.val;
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

}
