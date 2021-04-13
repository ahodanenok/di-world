package ahodanenok.di.metadata;

import ahodanenok.di.util.NamedQualifier;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

public class FieldMetadataReader {

    private final Field field;

    public FieldMetadataReader(Field field) {
        this.field = field;
    }

    /**
     * Field is injectable if it is annotated with @Inject annotation
     */
    public boolean readInjectable() {
        return field.isAnnotationPresent(Inject.class);
    }

    /**
     * Read @Named qualifier's value
     * @return value (never blank) or null if @Named is not present
     */
    private String readNamed() {
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation.annotationType().equals(Named.class)) {
                Named named = (Named) annotation;
                String name = named.value().trim();

                // https://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#named_at_injection_point
                // If an injected field declares a @Named annotation that does not specify
                // the value member, the name of the field is assumed.
                if (name.isEmpty()) {
                    name = field.getName();
                }

                return name;
            }
        }

        return null;
    }

    public List<Annotation> readQualifiers() {
        List<Annotation> qualifiers = ReflectionUtils.getAnnotations(field, ReflectionUtils.QUALIFIER_PREDICATE);

        String named = readNamed();
        if (named != null) {
            qualifiers.add(new NamedQualifier(named));
        }

        return qualifiers;
    }
}
