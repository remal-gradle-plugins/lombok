package name.remal.gradleplugins.lombok.config;

import static name.remal.gradleplugins.lombok.config.LombokConfig.LOMBOK_CONFIG_FILE_NAME;
import static name.remal.gradleplugins.lombok.config.LombokConfigNormalizer.normalizeLombokConfigKey;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.CLEAR;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.MINUS;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.PLUS;
import static name.remal.gradleplugins.lombok.config.LombokConfigPropertyOperator.SET;

import java.io.Serializable;
import javax.inject.Inject;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

@Getter
@Setter
public abstract class LombokExtensionConfigGenerate {

    public abstract Property<Boolean> getEnabled();

    public abstract RegularFileProperty getFile();

    {
        getFile().convention(getProjectLayout().getProjectDirectory().file(LOMBOK_CONFIG_FILE_NAME));
    }


    public abstract ListProperty<ConfigProperty> getProperties();

    public void set(String key, Object value) {
        getProperties().add(ConfigProperty.builder()
            .key(key)
            .operator(SET)
            .value(String.valueOf(value))
            .build()
        );
    }

    public void plus(String key, Object value) {
        getProperties().add(ConfigProperty.builder()
            .key(key)
            .operator(PLUS)
            .value(String.valueOf(value))
            .build()
        );
    }

    public void minus(String key, Object value) {
        getProperties().add(ConfigProperty.builder()
            .key(key)
            .operator(MINUS)
            .value(String.valueOf(value))
            .build()
        );
    }

    public void clear(String key) {
        getProperties().add(ConfigProperty.builder()
            .key(key)
            .operator(CLEAR)
            .value("")
            .build()
        );
    }

    @Value
    @Builder
    public static class ConfigProperty implements Serializable {
        String key;
        LombokConfigPropertyOperator operator;
        String value;

        public ConfigProperty(String key, LombokConfigPropertyOperator operator, String value) {
            this.key = normalizeLombokConfigKey(key);
            this.operator = operator;
            this.value = value;
        }
    }


    @Inject
    protected abstract ProjectLayout getProjectLayout();

}
