package ahodanenok.di.character.classes;

import javax.inject.Qualifier;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Repeatable(Ingredients.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ingredient {
    String name();
}
