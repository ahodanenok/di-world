package ahodanenok.di.metadata;

import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.interceptor.InterceptorType;
import ahodanenok.di.metadata.classes.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClassMetadataReaderTest {

    @Test
    @DisplayName("should return null as the name given class doesn't have @Named annotation")
    public void notNamed() {
        assertThat(new ClassMetadataReader<>(Banana.class).readNamed()).isNull();
    }

    @Test
    @DisplayName("should not inherit name from @Named in parent")
    public void notNamedByParent() {
        assertThat(new ClassMetadataReader<>(Trunk.class).readNamed()).isNull();
    }

    @Test
    @DisplayName("should return default name given @Named with a blank value")
    public void defaultNamed() {
        assertThat(new ClassMetadataReader<>(Spruce.class).readNamed()).isEqualTo("spruce");
    }

    @Test
    @DisplayName("should return name defined in @Named annotation")
    public void named() {
        assertThat(new ClassMetadataReader<>(Oak.class).readNamed()).isEqualTo("I'm an Oak!");
    }

    @Test
    @DisplayName("should return null as scope given class doesn't have any @Scope annotation ")
    public void notScoped() {
        assertThat(new ClassMetadataReader<>(Tree.class).readScope()).isNull();
    }

    @Test
    @DisplayName("should not inherited scope from parent given it's not inheritable")
    public void notInheritedScope() {
        assertThat(new ClassMetadataReader<>(Yggdrasil.class).readScope()).isNull();
    }

    @Test
    @DisplayName("should inherit scope from parent given it's inheritable and class doesn't define any")
    public void inheritedScope() {
        assertThat(new ClassMetadataReader<>(Trunk.class).readScope()).isEqualTo(PerTree.class.getName());
    }

    @Test
    @DisplayName("should not inherit scope from parent given scope inheritable but class defines own scope")
    public void notInheritedScopeIfDefined() {
        assertThat(new ClassMetadataReader<>(Leaf.class).readScope()).isEqualTo(PerBranch.class.getName());
    }

    @Test
    @DisplayName("should return singleton scope given class has a @Singleton annotation")
    public void singletonScope() {
        assertThat(new ClassMetadataReader<>(WorldTree.class).readScope()).isEqualTo(Singleton.class.getName());
    }

    @Test
    @DisplayName("should read custom scope given class is annotated with it")
    public void customScope() {
        assertThat(new ClassMetadataReader<>(Spruce.class).readScope()).isEqualTo(Evergreen.class.getName());
    }

    @Test
    @DisplayName("should throw error given multiple scopes are declared")
    public void multipleScopes() {
        assertThatThrownBy(() -> new ClassMetadataReader<>(Banana.class).readScope())
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessageStartingWith("Multiple scopes");
    }

    @Test
    @DisplayName("should throw error given scope has attributes")
    public void scopeAttributes() {
        assertThatThrownBy(() -> new ClassMetadataReader<>(Oak.class).readScope())
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessageStartingWith("Scope annotation must not declare any attributes");
    }

    @Test
    @DisplayName("should return true if class is interceptor")
    public void interceptor() {
        assertThat(new ClassMetadataReader<>(Forest.class).readInterceptor()).isTrue();
    }

    @Test
    @DisplayName("should return false if class is not interceptor")
    public void notInterceptor() {
        assertThat(new ClassMetadataReader<>(Oak.class).readInterceptor()).isFalse();
    }

    @Test
    @DisplayName("should return no interceptors if class doesn't define any")
    public void noInterceptors() {
        assertThat(new ClassMetadataReader<>(Tree.class).readInterceptors()).isEmpty();
    }

    @Test
    @DisplayName("should return one interceptor if class defines one")
    public void singleInterceptor() {
        assertThat(new ClassMetadataReader<>(Oak.class).readInterceptors()).containsExactly(Forest.class);
    }

    @Test
    @DisplayName("should return multiple interceptors in the order they are defined in")
    public void multipleInterceptors() {
        assertThat(new ClassMetadataReader<>(Spruce.class).readInterceptors())
                .containsExactly(Soil.class, Forest.class, Seasons.class);
    }

    @Test
    @DisplayName("should find @AroundInvoke interceptor in an interceptor class")
    public void aroundInvokeInInterceptor() throws Exception {
        assertThat(new ClassMetadataReader<>(Forest.class).readInterceptorMethod(InterceptorType.AROUND_INVOKE))
                .isEqualTo(Forest.class.getDeclaredMethod("onTreeGrowing"));
    }

    @Test
    @DisplayName("should find @AroundConstruct interceptor in an interceptor class")
    public void aroundConstructInInterceptor() throws Exception {
        assertThat(new ClassMetadataReader<>(Forest.class).readInterceptorMethod(InterceptorType.AROUND_CONSTRUCT))
                .isEqualTo(Forest.class.getDeclaredMethod("onTreeCreated"));
    }

    @Test
    @DisplayName("should find @PreDestroy interceptor in an interceptor class")
    public void preDestroyInInterceptor() throws Exception {
        assertThat(new ClassMetadataReader<>(Forest.class).readInterceptorMethod(InterceptorType.PRE_DESTROY))
                .isEqualTo(Forest.class.getDeclaredMethod("beforeTreeDestroyed"));
    }

    @Test
    @DisplayName("should find @PostConstruct interceptor in an interceptor class")
    public void postConstructInInterceptor() throws Exception {
        assertThat(new ClassMetadataReader<>(Forest.class).readInterceptorMethod(InterceptorType.POST_CONSTRUCT))
                .isEqualTo(Forest.class.getDeclaredMethod("afterTreeCreated"));
    }

    @Test
    @DisplayName("should find interceptor in an interceptor's superclass")
    public void aroundInvokeInSuperclass() throws Exception {
        assertThat(new ClassMetadataReader<>(TropicalForest.class).readInterceptorMethod(InterceptorType.AROUND_INVOKE))
                .isEqualTo(Forest.class.getDeclaredMethod("onTreeGrowing"));
    }

    @Test
    @DisplayName("should throw error if multiple interceptors are found in an interceptor")
    public void multipleAroundInvoke() {
        assertThatThrownBy(() ->
                    new ClassMetadataReader<>(Soil.class).readInterceptorMethod(InterceptorType.POST_CONSTRUCT))
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessageStartingWith("Multiple interceptors of type" +
                        " 'InterceptorType(javax.annotation.PostConstruct)' are defined" +
                        " in a class 'ahodanenok.di.metadata.classes.Soil'");
    }

    @Test
    @DisplayName("should read no qualifiers")
    public void noQualifiers() {
        assertThat(new ClassMetadataReader<>(Leaf.class).readQualifiers()).isEmpty();
    }

    @Test
    @DisplayName("should read qualifiers from a class")
    @Named("spruce")
    public void qualifiersClass() throws Exception {
        assertThat(new ClassMetadataReader<>(Spruce.class).readQualifiers()).containsExactlyInAnyOrder(
                getClass().getDeclaredMethod("qualifiersClass").getDeclaredAnnotation(Named.class),
                Spruce.class.getDeclaredAnnotation(Needles.class),
                Spruce.class.getDeclaredAnnotation(Tall.class)
        );
    }

    @Test
    @DisplayName("should read qualifier from parent given it is @Inherited")
    public void qualifiersParent() {
        assertThat(new ClassMetadataReader<>(Yggdrasil.class).readQualifiers())
                .containsExactly(WorldTree.class.getDeclaredAnnotation(Epic.class));
    }

    @Test
    @DisplayName("should not read qualifiers from other qualifiers")
    public void qualifiersComposition() {
        assertThat(new ClassMetadataReader<>(Tree.class).readQualifiers()).containsExactly(
                Tree.class.getDeclaredAnnotation(Plant.class));
    }

    @Test
    @DisplayName("should read repeatable qualifiers from a class")
    public void qualifierRepeatable() {
        assertThat(new ClassMetadataReader<>(Banana.class).readQualifiers())
                .containsExactlyInAnyOrder(Banana.class.getAnnotationsByType(Habitat.class));
    }

    @Test
    @DisplayName("should read interceptor bindings")
    public void interceptorBindings() {
        assertThat(new ClassMetadataReader<>(Seasons.class).readInterceptorBindings()).containsExactlyInAnyOrder(
                Seasons.class.getDeclaredAnnotation(Winter.class),
                Winter.class.getDeclaredAnnotation(Ice.class),
                Winter.class.getDeclaredAnnotation(Snow.class));
    }

    @Test
    @DisplayName("should not read interceptor bindings")
    public void noInterceptorBindings() {
        assertThat(new ClassMetadataReader<>(Tree.class).readInterceptorBindings()).isEmpty();
    }
}
