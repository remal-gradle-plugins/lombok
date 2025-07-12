package name.remal.gradle_plugins.lombok.config;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.Unmodifiable;

@Value
@Builder
@With
public class LombokConfigFile implements WithFile, WithProperties {

    LombokConfigPath file;

    @Singular("importTraceElement")
    List<ImportTraceElement> importTrace;

    @Singular("importInstruction")
    List<ImportInstruction> importInstructions;

    @Singular("property")
    List<LombokConfigFileProperty> properties;

    @Singular("parseError")
    List<LombokConfigFileParseError> parseErrors;


    @Override
    public Stream<LombokConfigFileProperty> streamProperties() {
        return getProperties().stream();
    }


    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    boolean stopBubbling = calculateStopBubbling();

    private boolean calculateStopBubbling() {
        return getBoolean("config.stopBubbling", false);
    }


    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<ResolvedImport> resolvedImports = resolveImports();

    @Unmodifiable
    private List<ResolvedImport> resolveImports() {
        return getImportInstructions().stream()
            .map(ImportInstructionResolver::resolveImport)
            .collect(toUnmodifiableList());
    }


    @Override
    public String getSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(WithFile.super.getSource());

        List<ImportTraceElement> importTrace = getImportTrace();
        for (var i = importTrace.size() - 1; 0 <= i; --i) {
            ImportTraceElement element = importTrace.get(i);
            sb.append(" (imported at ").append(element.getSource());
        }
        sb.append(")".repeat(importTrace.size()));

        return sb.toString();
    }

}
