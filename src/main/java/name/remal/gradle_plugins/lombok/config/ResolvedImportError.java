package name.remal.gradle_plugins.lombok.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class ResolvedImportError implements ResolvedImport {

    public static ResolvedImportErrorBuilder builderFor(ImportInstruction instruction) {
        return builder()
            .file(instruction.getFile())
            .lineNumber(instruction.getLineNumber())
            .value(instruction.getValue())
            ;
    }


    LombokConfigPath file;

    int lineNumber;

    String value;

    String message;


    @Override
    public String toString() {
        return "Import error at " + getSource() + ": " + getMessage();
    }

}
