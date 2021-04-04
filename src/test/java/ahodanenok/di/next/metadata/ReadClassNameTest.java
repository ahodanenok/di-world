package ahodanenok.di.next.metadata;

import ahodanenok.di.metadata.ClassMetadataReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Named;

import static org.assertj.core.api.Assertions.*;

public class ReadClassNameTest {

    @Named("some tree")
    private static class Tree { }

    @Named("I am an Oak!")
    private static class Oak extends Tree { }

    @Named
    private static class Spruce extends Tree { }

    private static class Maple extends Tree { }

    private static class Banana { }

    @Test
    @DisplayName("should return null as the name given class doesn't have @Named annotation")
    public void notNamed() {
        assertThat(new ClassMetadataReader<>(Banana.class).readName()).isNull();
    }

    @Test
    @DisplayName("should return null as the name given parent has @Named annotation but not the class itself")
    public void notNamedByParent() {
        assertThat(new ClassMetadataReader<>(Maple.class).readName()).isNull();
    }

    @Test
    @DisplayName("should return empty string as the name given @Named with a blank value")
    public void defaultNamed() {
        assertThat(new ClassMetadataReader<>(Spruce.class).readName()).isEmpty();
    }

    @Test
    @DisplayName("should return name given it's specified in @Named annotation on the class itself")
    public void named() {
        assertThat(new ClassMetadataReader<>(Oak.class).readName()).isEqualTo("I am an Oak!");
    }
}
