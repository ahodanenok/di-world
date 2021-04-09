package ahodanenok.di.next.metadata;

import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.metadata.ClassMetadataReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

public class ReadClassScopeTest {

    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Evergreen { }

    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Seasonal {
        String name() default "summer";
    }

    @Singleton
    private interface Tree { }

    @Evergreen
    private static class Spruce implements Tree { }

    @Singleton
    private static class WorldTree implements Tree { }

    private static class Maple implements Tree { }

    private static class BananaPlant { }

    @Seasonal
    private static class SummerTree { }

    @Seasonal
    @Evergreen
    private static class Oak { }

    @Test
    @DisplayName("should return null as scope given class doesn't have any @Scope annotation ")
    public void notScoped() {
        assertThat(new ClassMetadataReader<>(BananaPlant.class).readScope()).isNull();
    }

    @Test
    @DisplayName("should return null as scope given parent has @Scope annotation but not the class itself")
    public void notScopedByParent() {
        assertThat(new ClassMetadataReader<>(Maple.class).readScope()).isNull();
    }

    @Test
    @DisplayName("should read singleton scope")
    public void singleton() {
        assertThat(new ClassMetadataReader<>(WorldTree.class).readScope()).isEqualTo(Singleton.class.getName());
    }

    @Test
    @DisplayName("should read custom scope")
    public void custom() {
        assertThat(new ClassMetadataReader<>(Spruce.class).readScope()).isEqualTo(Evergreen.class.getName());
    }

    @Test
    @DisplayName("should throw error given multiple scopes are declared")
    public void multiple() {
        assertThatThrownBy(() -> new ClassMetadataReader<>(Oak.class).readScope())
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessageStartingWith("Multiple scopes");
    }

    @Test
    @DisplayName("should throw error given scope has attributes")
    public void attributes() {
        assertThatThrownBy(() -> new ClassMetadataReader<>(SummerTree.class).readScope())
                .isExactlyInstanceOf(CharacterMetadataException.class)
                .hasMessageStartingWith("Scope annotation must not declare any attributes");
    }
}
