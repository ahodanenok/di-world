package ahodanenok.di.metadata;

import javax.inject.Inject;
import java.lang.reflect.Field;

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
}
