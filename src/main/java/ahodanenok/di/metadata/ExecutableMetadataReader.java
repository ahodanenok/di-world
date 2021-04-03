package ahodanenok.di.metadata;

import javax.inject.Inject;
import java.lang.reflect.Executable;

public class ExecutableMetadataReader {

    private final Executable executable;

    public ExecutableMetadataReader(Executable executable) {
        this.executable = executable;
    }

    /**
     * Executable is injectable if it is annotated with @Inject annotation
     */
    public boolean readInjectable() {
        return executable.isAnnotationPresent(Inject.class);
    }
}
