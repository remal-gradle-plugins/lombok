package name.remal.gradle_plugins.lombok.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ImportInstruction implements WithFileLine {

    LombokConfigPath file;

    int lineNumber;

    String value;

}
