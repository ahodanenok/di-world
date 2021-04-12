package ahodanenok.di.metadata.classes;

import javax.inject.Qualifier;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Repeatable(Habitats.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Habitat {
    String location();
}
