package name.remal.gradle_plugins.lombok.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ImportTraceElement implements WithFileLine {

    public static ImportTraceElementBuilder builderFor(ResolvedImportFile resolvedImport) {
        return builder()
            .file(resolvedImport.getFile())
            .lineNumber(resolvedImport.getLineNumber())
            ;
    }


    LombokConfigPath file;

    int lineNumber;

}
