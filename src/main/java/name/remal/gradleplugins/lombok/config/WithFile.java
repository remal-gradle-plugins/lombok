package name.remal.gradleplugins.lombok.config;

interface WithFile {

    LombokConfigPath getFile();

    default String getSource() {
        return getFile().toString();
    }

}
