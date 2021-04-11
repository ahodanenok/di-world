package ahodanenok.di.next.metadata;

import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.metadata.ClassMetadataReader;
import ahodanenok.di.next.metadata.classes.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClassMetadataReaderTest {

    @Test
    @DisplayName("should return null as the name given class doesn't have @Named annotation")
    public void notNamed() {
        assertThat(new ClassMetadataReader<>(Banana.class).readName()).isNull();
    }

    @Test
    @DisplayName("should not inherit name from @Named in parent")
    public void notNamedByParent() {
        assertThat(new ClassMetadataReader<>(Trunk.class).readName()).isNull();
    }

    @Test
    @DisplayName("should return default name given @Named with a blank value")
    public void defaultNamed() {
        assertThat(new ClassMetadataReader<>(Spruce.class).readName()).isEqualTo("spruce");
    }

    @Test
    @DisplayName("should return name defined in @Named annotation")
    public void named() {
        assertThat(new ClassMetadataReader<>(Oak.class).readName()).isEqualTo("I'm an Oak!");
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
    @DisplayName("should find @AroundInvoke interceptor on an interceptor class")
    public void aroundInvokeInInterceptor() throws Exception {
        assertThat(new ClassMetadataReader<>(Forest.class).readInterceptorMethod(AroundInvoke.class.getName()))
                .isEqualTo(Forest.class.getDeclaredMethod("onTreeGrowing"));
    }

    @Test
    @DisplayName("should find @AroundConstruct interceptor on an interceptor class")
    public void aroundConstructInInterceptor() throws Exception {
        assertThat(new ClassMetadataReader<>(Forest.class).readInterceptorMethod(AroundConstruct.class.getName()))
                .isEqualTo(Forest.class.getDeclaredMethod("onTreeCreated"));
    }

    @Test
    @DisplayName("should find @PreDestroy interceptor on an interceptor class")
    public void preDestroyInInterceptor() throws Exception {
        assertThat(new ClassMetadataReader<>(Forest.class).readInterceptorMethod(PreDestroy.class.getName()))
                .isEqualTo(Forest.class.getDeclaredMethod("beforeTreeDestroyed"));
    }

    @Test
    @DisplayName("should find @PostConstruct interceptor on an interceptor class")
    public void postConstructInInterceptor() throws Exception {
        assertThat(new ClassMetadataReader<>(Forest.class).readInterceptorMethod(PostConstruct.class.getName()))
                .isEqualTo(Forest.class.getDeclaredMethod("afterTreeCreated"));
    }
}
