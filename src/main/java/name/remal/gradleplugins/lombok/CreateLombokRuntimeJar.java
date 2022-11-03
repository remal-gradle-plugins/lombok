package name.remal.gradleplugins.lombok;

import static java.util.Collections.singletonList;

import java.util.List;
import org.gradle.api.tasks.CacheableTask;

@CacheableTask
public abstract class CreateLombokRuntimeJar extends BaseCreateLombokJar {

    public CreateLombokRuntimeJar() {
        super("createRuntime");
    }

    @Override
    protected List<String> createArgs() {
        return singletonList("--create");
    }

    @Override
    protected String getGeneratedFileName() {
        return "lombok-runtime.jar";
    }

}
