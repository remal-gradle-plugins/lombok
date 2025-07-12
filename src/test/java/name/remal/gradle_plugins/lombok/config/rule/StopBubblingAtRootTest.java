package name.remal.gradle_plugins.lombok.config.rule;

import org.junit.jupiter.api.Test;

class StopBubblingAtRootTest
    extends AbstractLombokConfigRuleTest<StopBubblingAtRoot> {

    @Test
    void emptyConfig() {
        assertThatRuleViolated();
    }

    @Test
    void bubblingStoppedAtProject() {
        writeProjectLombokConfig("config.stopBubbling = true");
        assertThatRuleViolated();
    }

    @Test
    void bubblingStoppedAtRoot() {
        writeRootLombokConfig("config.stopBubbling = true");
        assertThatRuleNotViolated();
    }

}
