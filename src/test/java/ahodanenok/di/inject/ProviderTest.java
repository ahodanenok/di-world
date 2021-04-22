package ahodanenok.di.inject;

import ahodanenok.di.DefaultWorld;
import ahodanenok.di.ObjectRequest;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.inject.classes.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class ProviderTest {

    private static class ProviderCup {

        public ProviderCup() { }

        Provider<Drinkable> provider;

        @Inject
        void setDrinkable(Provider<Drinkable> provider) {
            this.provider = provider;
        }
    }

    @Test
    @DisplayName("should inject provider")
    public void injectProvider() {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().add(ClassCharacter.of(Cheese.class));
        w.getQueue().add(ClassCharacter.of(ProviderCup.class));
        w.getQueue().flush();

        ProviderCup cup = w.find(ObjectRequest.of(ProviderCup.class));
        assertThat(cup.provider).isNotNull();
        assertThat(cup.provider.get()).isExactlyInstanceOf(Water.class);
    }

    private static class ProviderProviderProviderCup {

        public ProviderProviderProviderCup() { }

        Provider<Provider<Provider<Drinkable>>> provider;

        @Inject
        void setDrinkable(Provider<Provider<Provider<Drinkable>>> provider) {
            this.provider = provider;
        }
    }

    @Test
    @DisplayName("should inject provider in provider in provider")
    public void injectProviderProviderProvider() {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().add(ClassCharacter.of(Butter.class));
        w.getQueue().add(ClassCharacter.of(ProviderProviderProviderCup.class));
        w.getQueue().flush();

        ProviderProviderProviderCup cup = w.find(ObjectRequest.of(ProviderProviderProviderCup.class));
        assertThat(cup.provider).isNotNull();
        assertThat(cup.provider.get().get().get()).isExactlyInstanceOf(Water.class);
    }

    private static class ProviderCollectionCup {

        public ProviderCollectionCup() { }

        Provider<Provider<Collection<Drinkable>>> provider;

        @Inject
        void setDrinkable(Provider<Provider<Collection<Drinkable>>> provider) {
            this.provider = provider;
        }
    }

    @Test
    @DisplayName("should inject provider with collection of all matched objects")
    public void providerCollection() {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().add(ClassCharacter.of(Tea.class));
        w.getQueue().add(ClassCharacter.of(Bread.class));
        w.getQueue().add(ClassCharacter.of(ProviderCollectionCup.class));
        w.getQueue().flush();

        ProviderCollectionCup cup = w.find(ObjectRequest.of(ProviderCollectionCup.class));
        assertThat(cup.provider).isNotNull();
        assertThat(cup.provider.get().get())
                .hasSize(2)
                .hasAtLeastOneElementOfType(Water.class)
                .hasAtLeastOneElementOfType(Tea.class);
    }

    private static class ProviderOptionalCup {

        public ProviderOptionalCup() { }

        Provider<Optional<Drinkable>> provider;

        @Inject
        void setDrinkable(Provider<Optional<Drinkable>> provider) {
            this.provider = provider;
        }
    }

    @Test
    @DisplayName("should inject provider with optional dependency")
    public void providerOptional() {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(ProviderOptionalCup.class));
        w.getQueue().flush();

        ProviderOptionalCup cup = w.find(ObjectRequest.of(ProviderOptionalCup.class));
        assertThat(cup.provider).isNotNull();
        assertThat(cup.provider.get().isPresent()).isFalse();
    }
}
