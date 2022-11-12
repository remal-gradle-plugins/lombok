package name.remal.gradleplugins.lombok.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class ImportTraceElement implements WithFileLine {

    public static ImportTraceElementBuilder builderFor(ResolvedImportFile resolvedImport) {
        return builder()
            .file(resolvedImport.getFile())
            .lineNumber(resolvedImport.getLineNumber())
            ;
    }


    LombokConfigPath file;

    int lineNumber;

}
