package name.remal.gradleplugins.lombok;

import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class LombokPluginTest {

    final Project project;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply(LombokPlugin.class);
    }

    @Test
    void test() {
        assertTrue(project.getPlugins().hasPlugin(LombokPlugin.class));
    }

}
