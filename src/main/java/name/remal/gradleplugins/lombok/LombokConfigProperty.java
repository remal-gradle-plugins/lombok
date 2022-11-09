package name.remal.gradleplugins.lombok;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class LombokConfigProperty {

    String name;

    LombokConfigPropertyOperator operator;

    String value;

}
