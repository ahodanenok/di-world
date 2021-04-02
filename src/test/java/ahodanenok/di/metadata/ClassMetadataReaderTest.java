package ahodanenok.di.metadata;

import ahodanenok.di.ClassMetadataReader;
import ahodanenok.di.ConfigException;
import org.junit.jupiter.api.Test;

import javax.inject.Named;
import javax.inject.Scope;
import javax.inject.Singleton;

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
}
