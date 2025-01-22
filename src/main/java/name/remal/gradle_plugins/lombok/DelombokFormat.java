package name.remal.gradle_plugins.lombok;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.getPropertyNameForGetter;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isGetterOf;

import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
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

        for (var method : DelombokFormat.class.getMethods()) {
            if (isGetterOf(method, Property.class)) {
                var property = (Property<?>) method.invoke(this);
                Object value = property.getOrNull();
                if (value == null) {
                    continue;
                }

                var name = getPropertyNameForGetter(method);

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


    @SneakyThrows
    @SuppressWarnings("unchecked")
    void convention(DelombokFormat other) {
        for (var method : DelombokFormat.class.getMethods()) {
            if (!isGetterOf(method, Property.class)) {
                continue;
            }

            var thisProperty = (Property<Object>) method.invoke(this);
            var otherProperty = (Property<Object>) method.invoke(other);
            if (thisProperty != null && otherProperty != null) {
                thisProperty.convention(otherProperty);
            }
        }
    }

}
