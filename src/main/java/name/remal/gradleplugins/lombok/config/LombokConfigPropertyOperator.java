package name.remal.gradleplugins.lombok.config;

public enum LombokConfigPropertyOperator {

    SET,
    PLUS,
    MINUS,
    CLEAR,
    ;

    public boolean isAuthoritative() {
        return this == SET || this == CLEAR;
    }

}
