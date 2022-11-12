package name.remal.gradleplugins.lombok.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LombokConfigFileParseError implements WithFileLine {

    LombokConfigPath file;

    int lineNumber;

    String message;


    @Override
    public String toString() {
        return "Parse error at " + getSource() + ": " + getMessage();
    }

}
