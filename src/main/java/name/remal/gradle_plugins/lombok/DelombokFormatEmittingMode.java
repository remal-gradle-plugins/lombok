package name.remal.gradle_plugins.lombok;

public enum DelombokFormatEmittingMode implements DelombokFormatValue {

    SKIP,
    GENERATE,
    ;

    @Override
    public String toArg() {
        return name().toLowerCase();
    }

}
