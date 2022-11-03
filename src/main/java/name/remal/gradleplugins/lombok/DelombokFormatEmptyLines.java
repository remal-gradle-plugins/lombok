package name.remal.gradleplugins.lombok;

public enum DelombokFormatEmptyLines implements DelombokFormatValue {

    INDENT,
    BLANK,
    ;

    @Override
    public String toArg() {
        return name().toLowerCase();
    }

}
