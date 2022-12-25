package name.remal.gradle_plugins.lombok.config;

interface WithFile {

    LombokConfigPath getFile();

    default String getSource() {
        return getFile().toString();
    }

}
