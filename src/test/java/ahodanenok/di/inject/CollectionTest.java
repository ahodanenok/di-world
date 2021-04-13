package ahodanenok.di.inject;

import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.exception.DependencyInjectionException;
import ahodanenok.di.inject.classes.Drinkable;
import ahodanenok.di.inject.classes.Tea;
import ahodanenok.di.inject.classes.Water;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class CollectionTest {

    private static class CollectionCup {

        public CollectionCup() { }

        List<Drinkable> collection;

        @Inject
        public void setCollection(List<Drinkable> collection) {
            this.collection = collection;
        }
    }

    @Test
    @DisplayName("should throw error on injecting collection of providers")
    public void injectCollection() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().add(ClassCharacter.of(Tea.class));
        w.getQueue().add(ClassCharacter.of(CollectionCup.class));
        w.getQueue().flush();

        CollectionCup cup = w.find(ObjectRequest.of(CollectionCup.class));
        assertThat(cup.collection).hasSize(2)
                .hasAtLeastOneElementOfType(Water.class)
                .hasAtLeastOneElementOfType(Tea.class);
    }

    private static class CollectionProviderCup {

        public CollectionProviderCup() { }

        List<Provider<Drinkable>> collection;

        @Inject
        public void setCollection(List<Provider<Drinkable>> collection) {
            this.collection = collection;
        }
    }

    @Test
    @DisplayName("should throw error on injecting collection of providers")
    public void injectCollectionProviders() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().add(ClassCharacter.of(Tea.class));
        w.getQueue().add(ClassCharacter.of(CollectionProviderCup.class));
        w.getQueue().flush();

        assertThatThrownBy(() -> w.find(ObjectRequest.of(CollectionProviderCup.class)))
                .isExactlyInstanceOf(DependencyInjectionException.class)
                .hasMessageStartingWith("Injecting collection of providers is not supported");

    }

    private static class CollectionOptionalCup {

        public CollectionOptionalCup() { }

        List<Optional<Drinkable>> collection;

        @Inject
        public void setCollection(List<Optional<Drinkable>> collection) {
            this.collection = collection;
        }
    }

    @Test
    @DisplayName("should throw error on injecting collection of optionals")
    public void injectCollectionOptionals() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().add(ClassCharacter.of(Tea.class));
        w.getQueue().add(ClassCharacter.of(CollectionOptionalCup.class));
        w.getQueue().flush();

        assertThatThrownBy(() -> w.find(ObjectRequest.of(CollectionOptionalCup.class)))
                .isExactlyInstanceOf(DependencyInjectionException.class)
                .hasMessageStartingWith("Injecting collection of optionals is not supported");

    }
}
