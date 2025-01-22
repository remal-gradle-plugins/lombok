package name.remal.gradle_plugins.lombok;

import java.util.List;
import org.gradle.api.tasks.CacheableTask;

@CacheableTask
public abstract class CreateLombokRuntimeJar extends BaseCreateLombokJar {

    public CreateLombokRuntimeJar() {
        super("createRuntime");
    }

    @Override
    protected List<String> createArgs() {
        return List.of("--create");
    }

    @Override
    protected String getGeneratedFileName() {
        return "lombok-runtime.jar";
    }

}
