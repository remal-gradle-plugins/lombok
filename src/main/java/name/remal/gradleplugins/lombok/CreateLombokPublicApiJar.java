package name.remal.gradleplugins.lombok;

import org.gradle.api.tasks.CacheableTask;

@CacheableTask
public abstract class CreateLombokPublicApiJar extends BaseCreateLombokJar {

    public CreateLombokPublicApiJar() {
        super("publicApi");
    }

    @Override
    protected String getGeneratedFileName() {
        return "lombok-api.jar";
    }

}
