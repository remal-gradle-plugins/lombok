package name.remal.gradle_plugins.lombok.config;

import static com.google.common.jimfs.Configuration.unix;

import com.google.common.jimfs.Jimfs;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import java.nio.file.FileSystem;
import org.junit.jupiter.api.AfterEach;

public abstract class AbstractLombokConfigTest {

    protected final FileSystem fs = Jimfs.newFileSystem(unix());

    @AfterEach
    @OverridingMethodsMustInvokeSuper
    protected void afterEach() throws Throwable {
        fs.close();
    }

}
