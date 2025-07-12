package name.remal.gradle_plugins.lombok.config.rule;

import name.remal.gradle_plugins.lombok.config.LombokConfigUsageFlag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ConfigureVarUsageTest
    extends AbstractLombokConfigRuleTest<ConfigureVarUsage> {

    @Test
    void emptyConfig() {
        assertThatRuleViolated();
    }

    @ParameterizedTest
    @EnumSource(LombokConfigUsageFlag.class)
    void settingSet(LombokConfigUsageFlag usage) {
        writeProjectLombokConfig("lombok.var.flagUsage = " + usage);
        assertThatRuleNotViolated();
    }

}
