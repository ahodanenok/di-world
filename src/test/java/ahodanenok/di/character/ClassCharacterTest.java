package ahodanenok.di.character;

import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.character.classes.*;
import ahodanenok.di.inject.classes.Coffee;
import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.scope.SingletonScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Provider;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

public class ClassCharacterTest {

    @Test
    @DisplayName("should throw error if class is null")
    public void classIsNull() {
        assertThatThrownBy(() -> ClassCharacter.of(null))
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessage("Class can't be null");
    }

    @Test
    @DisplayName("should throw error if class is not instantiatable")
    public void classNotInstantiatable() {
        assertThatThrownBy(() -> ClassCharacter.of(Serializable.class))
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessage("Class 'java.io.Serializable' is not instantiatable");
    }

    @Test
    @DisplayName("should have no names")
    public void notNamed() {
        assertThat(ClassCharacter.of(Water.class).getNames()).isEmpty();
    }

    @Test
    @DisplayName("should have default name")
    public void defaultName() {
        assertThat(ClassCharacter.of(Milk.class).getNames()).containsExactly("milk");
    }

    @Test
    @DisplayName("should read class name from @Named annotation")
    public void named() {
        assertThat(ClassCharacter.of(Tea.class).getNames()).containsExactly("herbal tea");
    }

    @Test
    @DisplayName("should set a single explicit name")
    public void namesExplicitSingle() {
        ClassCharacter<Water> character = ClassCharacter.of(Water.class);

        character.knownAs("fresh");
        assertThat(character.getNames()).containsExactly("fresh");

        character.knownAs("clear");
        assertThat(character.getNames()).containsExactly("clear");
    }

    @Test
    @DisplayName("should set multiple explicit names")
    public void namesExplicitMultiple() {
        ClassCharacter<Water> character = ClassCharacter.of(Water.class);

        character.knownAs("fresh", "clear");
        assertThat(character.getNames()).containsExactlyInAnyOrder("fresh", "clear");

        character.knownAs("hot", "drinkable");
        assertThat(character.getNames()).containsExactlyInAnyOrder("hot", "drinkable");
    }

    @Test
    @DisplayName("should throw error no names are provided")
    public void errorNoNames() {
        assertThatThrownBy(() -> ClassCharacter.of(Water.class).knownAs())
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessage("Provide at least one name");
    }

    @Test
    @DisplayName("should throw error if name is null")
    public void errorNameNull() {
        assertThatThrownBy(() -> ClassCharacter.of(Water.class).knownAs((String) null))
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessage("Name can't be null");
    }

    @Test
    @DisplayName("should throw error if name is blank")
    public void errorNameBlank() {
        assertThatThrownBy(() -> ClassCharacter.of(Water.class).knownAs("   "))
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessage("Name can't be empty");
    }

    @Test
    @DisplayName("should override name from annotation")
    public void nameOverride() {
        assertThat(ClassCharacter.of(Tea.class).knownAs("black tea").getNames()).containsExactly("black tea");
    }

    @Test
    @DisplayName("should read class qualifiers declared on a class")
    public void qualifiers() {
        assertThat(ClassCharacter.of(Water.class).getQualifiers())
                .containsExactlyInAnyOrder(
                        Water.class.getDeclaredAnnotation(Cold.class),
                        Water.class.getDeclaredAnnotation(Tasteless.class));
    }

    @Test
    @DisplayName("should read class qualifiers declared on a class and its parent")
    public void qualifiersParent() {
        assertThat(ClassCharacter.of(OrangeJuice.class).getQualifiers())
                .containsExactlyInAnyOrder(
                        Juice.class.getDeclaredAnnotation(Fresh.class),
                        OrangeJuice.class.getDeclaredAnnotation(Vitamin.class));
    }

    @Test
    @DisplayName("should read repeatable qualifiers")
    public void qualifiersRepeatable() {
        assertThat(ClassCharacter.of(GingerTea.class).getQualifiers())
                .containsExactlyInAnyOrder(GingerTea.class.getDeclaredAnnotationsByType(Ingredient.class));
    }

    @Test
    @DisplayName("should override qualifiers declared with annotations")
    @Fresh
    public void qualifiersOverride() throws Exception {
        Method m = getClass().getDeclaredMethod("qualifiersOverride");
        assertThat(ClassCharacter.of(Water.class).qualifiedAs(m.getDeclaredAnnotation(Fresh.class)).getQualifiers())
                .containsExactly(m.getDeclaredAnnotation(Fresh.class));
    }

    @Test
    @DisplayName("should explicitly set qualifiers")
    @Fresh
    @Vitamin("many")
    public void qualifiersExplicitly() throws Exception {
        Method m = getClass().getDeclaredMethod("qualifiersExplicitly");
        assertThat(ClassCharacter.of(Smoothie.class)
                    .qualifiedAs(m.getDeclaredAnnotation(Fresh.class), m.getDeclaredAnnotation(Vitamin.class))
                    .getQualifiers())
                .containsExactlyInAnyOrder(m.getDeclaredAnnotation(Fresh.class), m.getDeclaredAnnotation(Vitamin.class));
    }

    @Test
    @DisplayName("should throw error no qualifiers are provided")
    public void errorNoQualifiers() {
        assertThatThrownBy(() -> ClassCharacter.of(Water.class).qualifiedAs((Collection<Annotation>) null))
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessage("Provide at least one qualifier");
    }

    @Test
    @DisplayName("should throw error if a qualifier is null")
    public void errorQualifierNull() {
        assertThatThrownBy(() -> ClassCharacter.of(Water.class).qualifiedAs((Annotation) null))
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessage("Qualifier can't be null");
    }

    @Test
    @DisplayName("should get interceptors from @Interceptors annotation")
    public void interceptors() {
        assertThat(ClassCharacter.of(Water.class).getInterceptors())
                .containsExactly(Filter.class, ConsumptionTracker.class);
    }

    @Test
    @DisplayName("should find no interceptors given class doesn't have any")
    public void noInterceptors() {
        assertThat(ClassCharacter.of(OrangeJuice.class).getInterceptors()).isEmpty();
    }

    @Test
    @DisplayName("should override interceptors from annotation")
    public void interceptorsOverride() {
        assertThat(ClassCharacter.of(Water.class).interceptedBy(Boiler.class).getInterceptors())
                .containsExactly(Boiler.class);
    }

    @Test
    @DisplayName("should set interceptors explicitly")
    public void interceptorsExplicitly() {
        assertThat(ClassCharacter.of(OrangeJuice.class).interceptedBy(ConsumptionTracker.class).getInterceptors())
                .containsExactly(ConsumptionTracker.class);
    }

    @Test
    @DisplayName("should find @PreDestroy method")
    public void preDestroy() throws Exception {
        assertThat(ClassCharacter.of(ConsumptionTracker.class).getInterceptorMethod(PreDestroy.class.getName()))
                .isEqualTo(ConsumptionTracker.class.getDeclaredMethod("preDestroy", InvocationContext.class));
    }

    @Test
    @DisplayName("should find @PostConstruct method")
    public void postConstruct() throws Exception {
        assertThat(ClassCharacter.of(ConsumptionTracker.class).getInterceptorMethod(PostConstruct.class.getName()))
                .isEqualTo(ConsumptionTracker.class.getDeclaredMethod("postConstruct"));
    }

    @Test
    @DisplayName("should find @AroundConstruct method")
    public void aroundConstruct() throws Exception {
        assertThat(ClassCharacter.of(ConsumptionTracker.class).getInterceptorMethod(AroundConstruct.class.getName()))
                .isEqualTo(ConsumptionTracker.class.getDeclaredMethod("aroundConstruct"));
    }

    @Test
    @DisplayName("should find @AroundInvoke method")
    public void aroundInvoke() throws Exception {
        assertThat(ClassCharacter.of(ConsumptionTracker.class).getInterceptorMethod(AroundInvoke.class.getName()))
                .isEqualTo(ConsumptionTracker.class.getDeclaredMethod("aroundInvoke", InvocationContext.class));
    }

    @Test
    @DisplayName("should not find interceptor method if it is not defined")
    public void noInterceptorMethod() throws Exception {
        assertThat(ClassCharacter.of(Boiler.class).getInterceptorMethod(PostConstruct.class.getName())).isNull();
    }

    @Test
    @DisplayName("should set interceptor method explicitly")
    public void interceptorMethodExplicitly() throws Exception {
        assertThat(ClassCharacter.of(Boiler.class)
                    .intercepts(PostConstruct.class.getName(), "healthCheck")
                    .getInterceptorMethod(PostConstruct.class.getName()))
                .isEqualTo(Boiler.class.getDeclaredMethod("healthCheck"));
    }

    @Test
    @DisplayName("should have AlwaysNew scope given no scope is set")
    public void scopeAlwaysNew() {
        assertThat(ClassCharacter.of(Boiler.class).getScope()).isExactlyInstanceOf(AlwaysNewScope.class);
    }

    @Test
    @DisplayName("should be set as singleton by annotation")
    public void scopeSingletonAnnotation() {
        assertThat(ClassCharacter.of(Cooler.class).getScope()).isExactlyInstanceOf(SingletonScope.class);
    }

    @Test
    @DisplayName("should set singleton scope explicitly")
    public void scopeSingletonExplicit() {
        assertThat(ClassCharacter.of(Cooler.class).scopedBy(new SingletonScope<>()).getScope())
                .isExactlyInstanceOf(SingletonScope.class);
    }

    @Test
    @DisplayName("should override scope from annotation")
    public void scopeOverride() {
        class CustomScope<T> implements Scope<T> {
            @Override
            public T getObject(Provider<T> provider) {
                return provider.get();
            }
        }

        assertThat(ClassCharacter.of(Cooler.class).scopedBy(new CustomScope<>()).getScope())
                .isExactlyInstanceOf(CustomScope.class);
    }

    @Test
    @DisplayName("should read interceptor bindings")
    public void interceptorBindings() {
        assertThat(ClassCharacter.of(Tea.class).getInterceptorBindings())
                .containsExactly(Tea.class.getDeclaredAnnotation(MakeHot.class));
    }

    @Test
    @DisplayName("should not read interceptor bindings")
    public void noInterceptorBindings() {
        assertThat(ClassCharacter.of(Coffee.class).getInterceptorBindings()).isEmpty();
    }
}
