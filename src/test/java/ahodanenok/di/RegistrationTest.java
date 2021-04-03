package ahodanenok.di;

import ahodanenok.di.character.ClassCharacter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class RegistrationTest {

    public static class A { }

    @Test
    public void shouldRegisterContainerAfterFlush() {
        ClassCharacter<A> config = ClassCharacter.of(A.class);

        World w = new World();
        assertThat(w).hasSize(0);

        w.getQueue().add(config);
        assertThat(w).hasSize(0);

        w.getQueue().flush();
        assertThat(w).hasSize(1).doesNotContainNull();
    }

    @Test
    public void shouldRegisterContainerWithTypeFromConfiguration() {
        ClassCharacter<A> config = ClassCharacter.of(A.class);

        World w = new World();
        w.getQueue().add(config);
        w.getQueue().flush();

        assertThat(w.iterator().next().getObjectClass()).isEqualTo(A.class);
    }


    @Test
    public void shouldRegisterContainerWithNamesFromConfiguration() {
        ClassCharacter<A> config = ClassCharacter.of(A.class).knownAs("a", "b", "a", "c");

        World w = new World();
        w.getQueue().add(config);
        w.getQueue().flush();

        assertThat(w.iterator().next().getNames()).containsOnly("a", "b", "c");
    }

    @Test
    public void shouldThrowErrorIfContainerWithTheSameNameAlreadyRegistered() {
        ClassCharacter<A> configA1 = ClassCharacter.of(A.class).knownAs("a1", "a");
        ClassCharacter<A> configA2 = ClassCharacter.of(A.class).knownAs("a2", "a");

        World w = new World();
        w.getQueue().add(configA1);
        w.getQueue().add(configA2);

        assertThatThrownBy(() -> w.getQueue().flush()).isInstanceOf(IllegalStateException.class).hasMessage("a");
    }
}
