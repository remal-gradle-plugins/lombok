package name.remal.gradle_plugins.lombok.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class ResolvedImportFile implements ResolvedImport {

    public static ResolvedImportFileBuilder builderFor(ImportInstruction instruction) {
        return builder()
            .file(instruction.getFile())
            .lineNumber(instruction.getLineNumber())
            .value(instruction.getValue())
            ;
    }


    LombokConfigPath file;

    int lineNumber;

    String value;

    LombokConfigPath fileToImport;

}
