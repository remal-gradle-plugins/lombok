package name.remal.gradle_plugins.lombok;

public enum DelombokFormatEmptyLines implements DelombokFormatValue {

    INDENT,
    BLANK,
    ;

    @Override
    public String toArg() {
        return name().toLowerCase();
    }

}
