package name.remal.gradle_plugins.lombok.config;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;
import lombok.With;

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

    private List<ResolvedImport> resolveImports() {
        return unmodifiableList(
            getImportInstructions().stream()
                .map(ImportInstructionResolver::resolveImport)
                .collect(toList())
        );
    }


    @Override
    public String getSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(WithFile.super.getSource());

        List<ImportTraceElement> importTrace = getImportTrace();
        for (int i = importTrace.size() - 1; 0 <= i; --i) {
            ImportTraceElement element = importTrace.get(i);
            sb.append(" (imported at ").append(element.getSource());
        }
        for (int i = importTrace.size() - 1; 0 <= i; --i) {
            sb.append(')');
        }

        return sb.toString();
    }

}
