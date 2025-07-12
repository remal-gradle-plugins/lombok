package name.remal.gradle_plugins.lombok.config.rule;

import name.remal.gradle_plugins.lombok.config.LombokConfigUsageFlag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ConfigureValUsageTest
    extends AbstractLombokConfigRuleTest<ConfigureValUsage> {

    @Test
    void emptyConfig() {
        assertThatRuleViolated();
    }

    @ParameterizedTest
    @EnumSource(LombokConfigUsageFlag.class)
    void settingSet(LombokConfigUsageFlag usage) {
        writeProjectLombokConfig("lombok.val.flagUsage = " + usage);
        assertThatRuleNotViolated();
    }

}
