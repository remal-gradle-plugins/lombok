package name.remal.gradleplugins.lombok;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;

@RequiredArgsConstructor
class LombokPluginTest {

    final Project project;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply(LombokPlugin.class);
        project.getPluginManager().apply("java");
    }

}
