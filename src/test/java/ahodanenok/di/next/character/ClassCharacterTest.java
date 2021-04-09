package ahodanenok.di.next.character;

import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.next.character.classes.Milk;
import ahodanenok.di.next.character.classes.Tea;
import ahodanenok.di.next.inject.classes.Water;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.annotation.Annotation;
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

    }

    @Test
    @DisplayName("should read class qualifiers declared on a class and its parent")
    public void qualifiersParent() {

    }

    @Test
    @DisplayName("should read repeatable qualifiers")
    public void qualifiersRepeatable() {

    }

    @Test
    @DisplayName("should override qualifiers declared with annotations")
    public void qualifiersOverride() {

    }

    @Test
    @DisplayName("should explicitly set qualifiers")
    public void qualifiersExplicitly() {

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
}
