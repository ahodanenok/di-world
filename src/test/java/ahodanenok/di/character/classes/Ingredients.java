package ahodanenok.di.character.classes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Ingredients {
    Ingredient[] value();
}
