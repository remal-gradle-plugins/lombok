package name.remal.gradleplugins.lombok;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.getPropertyNameForGetter;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.isGetterOf;

import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public abstract class DelombokFormat {

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getPretty();


    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<String> getIndent();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<DelombokFormatEmptyLines> getEmptyLines();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<DelombokFormatEmittingMode> getFinalParams();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<DelombokFormatEmittingMode> getConstructorProperties();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<DelombokFormatEmittingMode> getSuppressWarnings();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<DelombokFormatEmittingMode> getGenerated();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<DelombokFormatEmittingMode> getDanceAroundIdeChecks();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<DelombokFormatEmittingMode> getGenerateDelombokComment();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<DelombokFormatEmittingMode> getJavaLangAsFQN();


    @SneakyThrows
    List<String> toArgs() {
        List<String> args = new ArrayList<>();

        for (val method : DelombokFormat.class.getMethods()) {
            if (isGetterOf(method, Property.class)) {
                val property = (Property<?>) method.invoke(this);
                Object value = property.getOrNull();
                if (value == null) {
                    continue;
                }

                val name = getPropertyNameForGetter(method);

                if (value instanceof Boolean) {
                    if (TRUE.equals(value)) {
                        args.add(format("--format=%s", name));
                    }
                } else if (value instanceof DelombokFormatValue) {
                    args.add(format("--format=%s:%s", name, ((DelombokFormatValue) value).toArg()));
                } else {
                    args.add(format("--format=%s:%s", name, value));
                }
            }
        }

        return args;
    }

}
