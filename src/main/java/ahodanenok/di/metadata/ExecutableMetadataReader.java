package ahodanenok.di.metadata;

import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.util.NamedQualifier;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.List;

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

    /**
     * Read @Named qualifier's value
     * @return value (never blank) or null if @Named is not present
     * @throws CharacterMetadataException if value is blank
     */
    private String readNamed(int paramNum) {
        for (Annotation annotation : executable.getParameterAnnotations()[paramNum]) {
            if (annotation.annotationType().equals(Named.class)) {
                Named named = (Named) annotation;
                String name = named.value().trim();

                // https://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#named_at_injection_point
                // If an injection point declares a @Named annotation that does not specify the value member,
                // the container automatically detects the problem and treats it as a definition error.
                if (name.isEmpty()) {
                    throw new CharacterMetadataException(String.format(
                            "@Named on a parameter %d must not have an empty value in '%s'",
                            paramNum, executable));
                }

                return name;
            }
        }

        return null;
    }

    /**
     * Find all @Qualifier annotations on the executable's parameter
     */
    public List<Annotation> readParameterQualifiers(int paramNum) {
        if (paramNum < 0 || paramNum >= executable.getParameterCount()) {
            throw new IllegalArgumentException(String.format(
                    "Executable '%s' doesn't have parameter at %d", executable, paramNum));
        }

        List<Annotation> qualifiers = ReflectionUtils.getAnnotations(
                executable.getParameters()[paramNum], ReflectionUtils.QUALIFIER_PREDICATE);

        String named = readNamed(paramNum);
        if (named != null) {
            qualifiers.add(new NamedQualifier(named));
        }

        return qualifiers;
    }
}
