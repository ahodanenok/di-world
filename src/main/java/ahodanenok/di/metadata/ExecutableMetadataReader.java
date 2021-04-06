package ahodanenok.di.metadata;

import ahodanenok.di.exception.ConfigException;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;

public class ExecutableMetadataReader {

    private final Executable executable;

    public ExecutableMetadataReader(Executable executable) {
        this.executable = executable;
    }

    public Executable getExecutable() {
        return executable;
    }

    /**
     * Executable is injectable if it is annotated with @Inject annotation
     */
    public boolean readInjectable() {
        return executable.isAnnotationPresent(Inject.class);
    }

    public String readParameterName(int paramNum) {
        for (Annotation annotation : executable.getParameterAnnotations()[paramNum]) {
            if (annotation.annotationType().equals(Named.class)) {
                Named named = (Named) annotation;
                String name = named.value().trim();
                if (name.isEmpty()) {
                    throw new ConfigException(String.format(
                            "@Named on a parameter %d must have not empty value in %s",
                            paramNum, executable));
                }

                return name;
            }
        }

        return null;
    }
}
