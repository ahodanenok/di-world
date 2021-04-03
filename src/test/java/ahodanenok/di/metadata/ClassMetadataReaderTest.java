package ahodanenok.di.metadata;

import ahodanenok.di.ConfigException;
import org.junit.jupiter.api.Test;

import javax.inject.Named;
import javax.inject.Scope;
import javax.inject.Singleton;
import javax.interceptor.Interceptor;
import javax.interceptor.Interceptors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

public class ClassMetadataReaderTest {

    static class NotNamedClass { }

    @Named
    static class DefaultNamedClass { }

    @Named("test name")
    static class NamedClass { }

    @Test
    public void shouldReadNullAsClassNameForClassNotAnnotatedWithNamed() {
        assertThat(new ClassMetadataReader<>(NotNamedClass.class).readName()).isNull();
    }

    @Test
    public void shouldReadEmptyStringAsClassNameForClassWithNamedWithoutExplicitName() {
        assertThat(new ClassMetadataReader<>(DefaultNamedClass.class).readName()).isEmpty();
    }

    @Test
    public void shouldReadEnteredClassNameInNamedAnnotation() {
        assertThat(new ClassMetadataReader<>(NamedClass.class).readName()).isEqualTo("test name");
    }

    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestScope { }

    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    @interface ScopeWithAttribute { String value() default ""; }

    static class NotScopedClass { }

    @Singleton
    static class ScopedClass { }

    @Singleton @TestScope
    static class MultipleScopesClass { }

    @ScopeWithAttribute("test")
    static class ScopedWithAttributes { }

    @Test
    public void shouldReadNoScope() {
        assertThat(new ClassMetadataReader<>(NotScopedClass.class).readScope()).isNull();
    }

    @Test
    public void shouldReadScope() {
        assertThat(new ClassMetadataReader<>(ScopedClass.class).readScope()).isEqualTo(Singleton.class.getName());
    }

    @Test
    public void shouldThrowErrorIfMultipleScopes() {
        assertThatThrownBy(() -> new ClassMetadataReader<>(MultipleScopesClass.class).readScope())
                .isExactlyInstanceOf(ConfigException.class)
                .hasMessageStartingWith("Multiple scopes");
    }

    @Test
    public void shouldThrowErrorIfScopeHasAttributes() {
        assertThatThrownBy(() -> new ClassMetadataReader<>(ScopedWithAttributes.class).readScope())
                .isExactlyInstanceOf(ConfigException.class)
                .hasMessageStartingWith("Scope annotation must not declare any attributes");
    }

    @Interceptor public static class InterceptorClass { }
    public static class NotInterceptorClass { }
    public static class InterceptorInParent extends InterceptorClass { }

    @Test
    public void shouldReadInterceptorAsTrue() {
        assertThat(new ClassMetadataReader<>(InterceptorClass.class).readInterceptor()).isTrue();
    }

    @Test
    public void shouldReadInterceptorAsFalse() {
        assertThat(new ClassMetadataReader<>(NotInterceptorClass.class).readInterceptor()).isFalse();
    }

    @Test
    public void shouldNotReadInterceptorAsTrueIfInParent() {
        assertThat(new ClassMetadataReader<>(InterceptorInParent.class).readInterceptor()).isFalse();
    }

    @Interceptors({ Integer.class, Long.class })
    public static class WithInterceptorsClass { }
    public static class WithoutInterceptorsClass { }
    public static class WithInterceptorsInParent extends WithInterceptorsClass { }

    @Test
    public void shouldReadInterceptors() {
        assertThat(new ClassMetadataReader<>(WithInterceptorsClass.class).readInterceptors())
                .containsExactly(Integer.class, Long.class);
    }

    @Test
    public void shouldNotReadInterceptors() {
        assertThat(new ClassMetadataReader<>(WithoutInterceptorsClass.class).readInterceptors()).isEmpty();
    }

    @Test
    public void shouldNotReadInterceptorInParent() {
        assertThat(new ClassMetadataReader<>(WithInterceptorsInParent.class).readInterceptors()).isEmpty();
    }
}
