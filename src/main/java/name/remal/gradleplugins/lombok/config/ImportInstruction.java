package name.remal.gradleplugins.lombok.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class ImportInstruction implements WithFileLine {

    LombokConfigPath file;

    int lineNumber;

    String value;

}
