package name.remal.gradleplugins.lombok.config;

interface WithFileLine extends WithFile {

    int getLineNumber();

    @Override
    default String getSource() {
        return WithFile.super.getSource() + ':' + getLineNumber();
    }

}
