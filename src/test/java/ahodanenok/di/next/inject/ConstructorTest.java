package ahodanenok.di.next.inject;

import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.exception.ConfigException;
import ahodanenok.di.next.inject.classes.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ConstructorTest {

    @Test
    @DisplayName("should instantiate class using public no-arg constructor")
    public void instantiate() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Bread.class));
        w.getQueue().flush();

        assertThat(w.find(ObjectRequest.of(Bread.class))).isExactlyInstanceOf(Bread.class);
    }

    @Test
    @DisplayName("should instantiate class using public multiple args constructor without @Inject annotation")
    public void injectWithoutInject() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Sandwich.class));
        w.getQueue().add(ClassCharacter.of(Bread.class));
        w.getQueue().add(ClassCharacter.of(Butter.class));
        w.getQueue().flush();

        Sandwich s = w.find(ObjectRequest.of(Sandwich.class));
        assertThat(s).isExactlyInstanceOf(Sandwich.class);
        assertThat(s.bread).isExactlyInstanceOf(Bread.class);
        assertThat(s.butter).isExactlyInstanceOf(Butter.class);
    }

    @Test
    @DisplayName("should throw error if there is no @Inject constructor and class doesn't have a single public constructor")
    public void instantiateNoConstructor() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Cheese.class));
        w.getQueue().flush();

        assertThatThrownBy(() -> w.find(ObjectRequest.of(Cheese.class)))
                .isExactlyInstanceOf(ConfigException.class)
                .hasMessageStartingWith("Couldn't resolve constructor");
    }

    @Test
    @DisplayName("should instantiate class using public constructor with @Inject annotation")
    public void injectPublicConstructor() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Breakfast.class));
        w.getQueue().add(ClassCharacter.of(Bread.class));
        w.getQueue().flush();

        Breakfast b = w.find(ObjectRequest.of(Breakfast.class));
        assertThat(b).isExactlyInstanceOf(Breakfast.class);
        assertThat(b.bread).isExactlyInstanceOf(Bread.class);
    }

    @Test
    @DisplayName("should instantiate class using protected constructor with @Inject annotation")
    public void injectProtectedConstructor() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Bread.class));
        w.getQueue().add(ClassCharacter.of(Lunch.class));
        w.getQueue().flush();

        Lunch lunch = w.find(ObjectRequest.of(Lunch.class));
        assertThat(lunch).isExactlyInstanceOf(Lunch.class);
        assertThat(lunch.bread).isExactlyInstanceOf(Bread.class);
    }

    @Test
    @DisplayName("should instantiate class using package-private constructor with @Inject annotation")
    public void injectPackagePrivateConstructor() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Dinner.class));
        w.getQueue().add(ClassCharacter.of(Bread.class));
        w.getQueue().flush();

        Dinner d = w.find(ObjectRequest.of(Dinner.class));
        assertThat(d).isExactlyInstanceOf(Dinner.class);
        assertThat(d.bread).isExactlyInstanceOf(Bread.class);
    }

    @Test
    @DisplayName("should instantiate class using private constructor with @Inject annotation")
    public void injectPrivateConstructor() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Supper.class));
        w.getQueue().add(ClassCharacter.of(Bread.class));
        w.getQueue().flush();

        Supper s = w.find(ObjectRequest.of(Supper.class));
        assertThat(s).isExactlyInstanceOf(Supper.class);
        assertThat(s.bread).isExactlyInstanceOf(Bread.class);
    }

    @Test
    @DisplayName("should throw error if class is an interface")
    public void instantiateInterface() {
        World w = new World();
        assertThatThrownBy(() -> w.getQueue().add(ClassCharacter.of(Drinkable.class)))
                .isExactlyInstanceOf(ConfigException.class)
                .hasMessageStartingWith("Can't instantiate an interface");
    }

    @Test
    @DisplayName("should throw error if class is an abstract class")
    public void abstractClass() {
        World w = new World();
        assertThatThrownBy(() -> w.getQueue().add(ClassCharacter.of(Drink.class)))
                .isExactlyInstanceOf(ConfigException.class)
                .hasMessageStartingWith("Can't instantiate an interface");
    }
}
