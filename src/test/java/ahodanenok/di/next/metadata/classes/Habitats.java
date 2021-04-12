package ahodanenok.di.next.metadata.classes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Habitats {
    Habitat[] value();
}
