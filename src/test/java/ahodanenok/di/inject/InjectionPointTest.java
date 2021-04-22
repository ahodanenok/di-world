package ahodanenok.di.inject;

import ahodanenok.di.DefaultWorld;
import ahodanenok.di.ObjectRequest;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.exception.DependencyInjectionException;
import ahodanenok.di.exception.DependencyLookupException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class InjectionPointTest {

    private static class Filler {
        public Filler() { }
    }

    private static class InjectionPointConstructor {

        Filler filler;
        InjectionPoint injectionPoint;

        @Inject
        InjectionPointConstructor(Filler filler, InjectionPoint injectionPoint) {
            this.filler = filler;
            this.injectionPoint = injectionPoint;
        }
    }

    private static class InjectionPointConstructorHolder {

        InjectionPointConstructor constructor;

        @Inject
        InjectionPointConstructorHolder(InjectionPointConstructor constructor) {
            this.constructor = constructor;
        }
    }

    @Test
    @DisplayName("should inject InjectionPoint into a constructor")
    public void injectInjectionPointIntoConstructor() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(Filler.class));
        w.getQueue().add(ClassCharacter.of(InjectionPointConstructorHolder.class));
        w.getQueue().add(ClassCharacter.of(InjectionPointConstructor.class));
        w.getQueue().flush();

        InjectionPointConstructorHolder holder = w.find(ObjectRequest.of(InjectionPointConstructorHolder.class));
        assertThat(holder.constructor).isNotNull();
        assertThat(holder.constructor.filler).isNotNull();
        assertThat(holder.constructor.injectionPoint).isNotNull();
        assertThat(holder.constructor.injectionPoint.getTarget()).isEqualTo(
                InjectionPointConstructorHolder.class.getDeclaredConstructor(InjectionPointConstructor.class));
    }

    private static class InjectionPointMethod {

        Filler filler;
        InjectionPoint injectionPoint;

        public InjectionPointMethod() { }

        @Inject
        void setInjectionPointMethod(Filler filler, InjectionPoint injectionPoint) {
            this.filler = filler;
            this.injectionPoint = injectionPoint;
        }
    }

    private static class InjectionPointMethodHolder {

        InjectionPointMethod method;

        public InjectionPointMethodHolder() { }

        @Inject
        void setInjectionPointMethodHolder(InjectionPointMethod constructor) {
            this.method = constructor;
        }
    }

    @Test
    @DisplayName("should inject InjectionPoint into a method")
    public void injectInjectionPointIntoMethod() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(Filler.class));
        w.getQueue().add(ClassCharacter.of(InjectionPointMethod.class));
        w.getQueue().add(ClassCharacter.of(InjectionPointMethodHolder.class));
        w.getQueue().flush();

        InjectionPointMethodHolder holder = w.find(ObjectRequest.of(InjectionPointMethodHolder.class));
        assertThat(holder.method).isNotNull();
        assertThat(holder.method.filler).isNotNull();
        assertThat(holder.method.injectionPoint).isNotNull();
        assertThat(holder.method.injectionPoint.getTarget()).isEqualTo(
                InjectionPointMethodHolder.class.getDeclaredMethod("setInjectionPointMethodHolder", InjectionPointMethod.class));
    }

    private static class InjectionPointField {

        @Inject
        Filler filler;

        @Inject
        InjectionPoint injectionPoint;

        public InjectionPointField() { }
    }

    private static class InjectionPointFieldHolder {

        @Inject
        InjectionPointField field;

        public InjectionPointFieldHolder() { }
    }

    @Test
    @DisplayName("should inject InjectionPoint into a method")
    public void injectInjectionPointIntoField() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(InjectionPointField.class));
        w.getQueue().add(ClassCharacter.of(InjectionPointFieldHolder.class));
        w.getQueue().add(ClassCharacter.of(Filler.class));
        w.getQueue().flush();

        InjectionPointFieldHolder holder = w.find(ObjectRequest.of(InjectionPointFieldHolder.class));
        assertThat(holder.field).isNotNull();
        assertThat(holder.field.filler).isNotNull();
        assertThat(holder.field.injectionPoint).isNotNull();
        assertThat(holder.field.injectionPoint.getTarget())
                .isEqualTo(InjectionPointFieldHolder.class.getDeclaredField("field"));
    }

    private static class InjectionPointProvider {

        @Inject
        Provider<InjectionPoint> injectionPoint;

        public InjectionPointProvider() { }
    }

    private static class InjectionPointProviderHolder {

        @Inject
        InjectionPointProvider field;

        public InjectionPointProviderHolder() { }
    }

    @Test
    @DisplayName("should throw error when retrieving InjectionPoint from provider")
    public void injectInjectionPointProvider() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(InjectionPointProvider.class));
        w.getQueue().add(ClassCharacter.of(InjectionPointProviderHolder.class));
        w.getQueue().flush();

        InjectionPointProviderHolder holder = w.find(ObjectRequest.of(InjectionPointProviderHolder.class));
        assertThat(holder.field).isNotNull();
        assertThat(holder.field.injectionPoint).isNotNull();
        assertThatThrownBy(() -> holder.field.injectionPoint.get())
                .isExactlyInstanceOf(DependencyInjectionException.class)
                .getCause().isExactlyInstanceOf(DependencyLookupException.class)
                .hasMessage("No active injection point");

    }

    private static class InjectionPointOptional {

        @Inject
        Optional<InjectionPoint> injectionPoint;

        public InjectionPointOptional() { }
    }

    private static class InjectionPointOptionalHolder {

        @Inject
        InjectionPointOptional field;

        public InjectionPointOptionalHolder() { }
    }

    @Test
    @DisplayName("should inject optional with InjectionPoint")
    public void injectInjectionPointOptional() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(InjectionPointOptional.class));
        w.getQueue().add(ClassCharacter.of(InjectionPointOptionalHolder.class));
        w.getQueue().flush();

        InjectionPointOptionalHolder holder = w.find(ObjectRequest.of(InjectionPointOptionalHolder.class));
        assertThat(holder.field).isNotNull();
        assertThat(holder.field.injectionPoint).isNotNull();
        assertThat(holder.field.injectionPoint.get().getTarget())
                .isEqualTo(InjectionPointOptionalHolder.class.getDeclaredField("field"));
    }

    private static class InjectionPointCollection {

        Set<InjectionPoint> injectionPoint;

        public InjectionPointCollection() { }

        @Inject
        void injectCollection(Set<InjectionPoint> injectionPoint) {
            this.injectionPoint = injectionPoint;
        }
    }

    private static class InjectionPointCollectionHolder {

        @Inject
        InjectionPointCollection field;

        public InjectionPointCollectionHolder() { }
    }

    @Test
    @DisplayName("should inject collection with a single item of InjectionPoint")
    public void injectInjectionPointCollection() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(InjectionPointCollectionHolder.class));
        w.getQueue().add(ClassCharacter.of(InjectionPointCollection.class));
        w.getQueue().flush();

        InjectionPointCollectionHolder holder = w.find(ObjectRequest.of(InjectionPointCollectionHolder.class));
        assertThat(holder.field).isNotNull();
        assertThat(holder.field.injectionPoint).hasSize(1);
        assertThat(holder.field.injectionPoint.iterator().next().getTarget())
                .isEqualTo(InjectionPointCollectionHolder.class.getDeclaredField("field"));
    }
}
