package ahodanenok.di.metadata;

import ahodanenok.di.exception.ConfigException;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
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

    public String readName() {
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation.annotationType().equals(Named.class)) {
                Named named = (Named) annotation;
                String name = named.value().trim();
                if (name.isEmpty()) {
                    name = field.getName();
                }

                return name;
            }
        }

        return null;
    }

    public List<Annotation> readQualifiers() {
        return ReflectionUtils.getAnnotations(field, a -> a.annotationType().isAnnotationPresent(Qualifier.class));
    }
}
