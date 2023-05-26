package name.remal.gradle_plugins.lombok.config.rule;

import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.lombok.config.LombokConfigurationKeys.getAllLombokConfigurationKeys;
import static name.remal.gradle_plugins.lombok.config.rule.AddGeneratedAnnotation.ADD_GENERATED_ANNOTATION_CONFIG_KEYS;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.endsWithString;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.startsWithString;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;
import name.remal.gradle_plugins.lombok.config.LombokConfigurationKey;
import org.junit.jupiter.api.Test;

class AddGeneratedAnnotationTest {

    @Test
    void allConfigKeysSupported() {
        val addGeneratedAnnotationCurrentConfigKeys = getAllLombokConfigurationKeys().stream()
            .map(LombokConfigurationKey::getName)
            .filter(startsWithString("lombok.add"))
            .filter(endsWithString("GeneratedAnnotation"))
            .collect(toList());
        assertThat(ADD_GENERATED_ANNOTATION_CONFIG_KEYS)
            .containsAll(addGeneratedAnnotationCurrentConfigKeys);
    }

}
