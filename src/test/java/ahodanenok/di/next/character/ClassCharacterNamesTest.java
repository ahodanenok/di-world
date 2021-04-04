package ahodanenok.di.next.character;

import ahodanenok.di.character.ClassCharacter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Named;

import static org.assertj.core.api.Assertions.*;

public class ClassCharacterNamesTest {

    @Named("water!!!")
    private static class Water { }

    @Named
    private static class Juice { }

    private static class Milk { }

    @Test
    @DisplayName("should return object class")
    public void objectClass() {
        assertThat(ClassCharacter.of(Water.class).getObjectClass()).isEqualTo(Water.class);
    }

    @Test
    @DisplayName("should have no name")
    public void notNamed() {
        assertThat(ClassCharacter.of(Milk.class).getNames()).isEmpty();
    }

    @Test
    @DisplayName("should assign default name")
    @Disabled // todo: implement default name
    public void defaultName() {
        assertThat(ClassCharacter.of(Juice.class).getNames()).containsExactly("juice");
    }

    @Test
    @DisplayName("should return name from annotation")
    public void named() {
        assertThat(ClassCharacter.of(Water.class).getNames()).containsExactly("water!!!");
    }

    @Test
    @DisplayName("should override name from annotation")
    public void namedOverride() {
        assertThat(ClassCharacter.of(Juice.class).knownAs("drinking water").getNames())
                .containsExactly("drinking water");
    }

    @Test
    @DisplayName("should override previously set names")
    public void previousNamesOverride() {
        assertThat(ClassCharacter.of(Juice.class)
                .knownAs("orange juice", "citrus juice")
                .knownAs("apple juice")
                .getNames()) .containsExactly("apple juice");
    }

    @Test
    @DisplayName("should return all set names")
    public void multipleNames() {
        assertThat(ClassCharacter.of(Milk.class)
                .knownAs("chocolate milk", "cacao milk", "sweetened milk")
                .getNames()).containsExactlyInAnyOrder("chocolate milk", "cacao milk", "sweetened milk");
    }
}
