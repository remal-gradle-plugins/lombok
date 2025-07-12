package name.remal.gradle_plugins.lombok.config.rule;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("java:S5976")
class DisableGlobalFieldDefaultsTest
    extends AbstractLombokConfigRuleTest<DisableGlobalFieldDefaults> {

    @Test
    void emptyConfig() {
        assertThatRuleNotViolated();
    }

    @ParameterizedTest
    @MethodSource("settingSet")
    void settingSet(String key, boolean enabled) {
        writeProjectLombokConfig(key + " = " + enabled);
        if (enabled) {
            assertThatRuleViolated();
        } else {
            assertThatRuleNotViolated();
        }
    }

    private static Stream<Arguments> settingSet() {
        return Stream.of(
                "lombok.fieldDefaults.defaultPrivate",
                "lombok.fieldDefaults.defaultFinal"
            )
            .flatMap(key -> Stream.of(
                Arguments.of(key, true),
                Arguments.of(key, false)
            ));
    }

}
