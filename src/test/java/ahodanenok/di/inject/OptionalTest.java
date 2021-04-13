package ahodanenok.di.inject;

import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.inject.classes.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalTest {

    private static class OptionalCup {

        public OptionalCup() { }

        Optional<Drinkable> optional;

        @Inject
        void setDrinkable(Optional<Drinkable> optional) {
            this.optional = optional;
        }
    }

    @Test
    @DisplayName("should inject not empty optional")
    public void injectNotEmptyOptional() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().add(ClassCharacter.of(OptionalCup.class));
        w.getQueue().flush();

        OptionalCup cup = w.find(ObjectRequest.of(OptionalCup.class));
        assertThat(cup.optional).isNotNull();
        assertThat(cup.optional.isPresent()).isTrue();
        assertThat(cup.optional.orElse(null)).isExactlyInstanceOf(Water.class);
    }

    @Test
    @DisplayName("should inject empty optional")
    public void injectEmptyProvider() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Cheese.class));
        w.getQueue().add(ClassCharacter.of(OptionalCup.class));
        w.getQueue().flush();

        OptionalCup cup = w.find(ObjectRequest.of(OptionalCup.class));
        assertThat(cup.optional).isNotNull();
        assertThat(cup.optional.isPresent()).isFalse();
    }

    private static class OptionalProviderCup {

        public OptionalProviderCup() { }

        Optional<Provider<Drinkable>> optional;

        @Inject
        void setDrinkable(Optional<Provider<Drinkable>> optional) {
            this.optional = optional;
        }
    }

    @Test
    @DisplayName("should inject not empty optional with provider")
    public void injectOptionalNotEmptyProvider() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().add(ClassCharacter.of(Butter.class));
        w.getQueue().add(ClassCharacter.of(OptionalProviderCup.class));
        w.getQueue().flush();

        OptionalProviderCup cup = w.find(ObjectRequest.of(OptionalProviderCup.class));
        assertThat(cup.optional).isNotNull();
        assertThat(cup.optional.isPresent()).isTrue();
        assertThat(cup.optional.get().get()).isExactlyInstanceOf(Water.class);
    }

    @Test
    @DisplayName("should inject not empty optional with empty provider")
    public void injectOptionalEmptyProvider() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Cheese.class));
        w.getQueue().add(ClassCharacter.of(OptionalProviderCup.class));
        w.getQueue().flush();

        OptionalProviderCup cup = w.find(ObjectRequest.of(OptionalProviderCup.class));
        assertThat(cup.optional).isNotNull();
        assertThat(cup.optional.isPresent()).isTrue();
        assertThat(cup.optional.get().get()).isNull();
    }

    private static class OptionalProviderCollectionCup {

        public OptionalProviderCollectionCup() { }

        Optional<Provider<Collection<Drinkable>>> optional;

        @Inject
        void setDrinkable(Optional<Provider<Collection<Drinkable>>> optional) {
            this.optional = optional;
        }
    }

    @Test
    @DisplayName("should inject optional with provider with not empty collection")
    public void injectOptionalProviderNotEmptyCollection() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().add(ClassCharacter.of(Coffee.class));
        w.getQueue().add(ClassCharacter.of(OptionalProviderCollectionCup.class));
        w.getQueue().flush();

        OptionalProviderCollectionCup cup = w.find(ObjectRequest.of(OptionalProviderCollectionCup.class));
        assertThat(cup.optional).isNotNull();
        assertThat(cup.optional.isPresent()).isTrue();
        assertThat(cup.optional.get().get()).isNotEmpty()
                .hasAtLeastOneElementOfType(Water.class)
                .hasAtLeastOneElementOfType(Coffee.class);
    }

    @Test
    @DisplayName("should inject optional with provider with empty collection")
    public void injectOptionalProviderEmptyCollection() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Bread.class));
        w.getQueue().add(ClassCharacter.of(OptionalProviderCollectionCup.class));
        w.getQueue().flush();

        OptionalProviderCollectionCup cup = w.find(ObjectRequest.of(OptionalProviderCollectionCup.class));
        assertThat(cup.optional).isNotNull();
        assertThat(cup.optional.isPresent()).isTrue();
        assertThat(cup.optional.get().get()).isEmpty();
    }
}
