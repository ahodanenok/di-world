package ahodanenok.di;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class RegistrationTest {

    public static class A { }

    @Test
    public void shouldRegisterContainerAfterFlush() {
        ContainerConfiguration config = ContainerConfiguration.ofClass(A.class);

        World w = new World();
        assertThat(w).hasSize(0);

        w.getQueue().add(config);
        assertThat(w).hasSize(0);

        w.getQueue().flush();
        assertThat(w).hasSize(1).doesNotContainNull();
    }

    @Test
    public void shouldRegisterContainerWithTypeFromConfiguration() {
        ContainerConfiguration config = ContainerConfiguration.ofClass(A.class);

        World w = new World();
        w.getQueue().add(config);
        w.getQueue().flush();

        assertThat(w.iterator().next().getType()).isEqualTo(A.class);
    }


    @Test
    public void shouldRegisterContainerWithNamesFromConfiguration() {
        ContainerConfiguration config = ContainerConfiguration.ofClass(A.class).withNames("a", "b", "a", "c");

        World w = new World();
        w.getQueue().add(config);
        w.getQueue().flush();

        assertThat(w.iterator().next().getNames()).containsOnly("a", "b", "c");
    }

    @Test
    public void shouldThrowErrorIfContainerWithTheSameNameAlreadyRegistered() {
        ContainerConfiguration configA1 = ContainerConfiguration.ofClass(A.class).withNames("a1", "a");
        ContainerConfiguration configA2 = ContainerConfiguration.ofClass(A.class).withNames("a2", "a");

        World w = new World();
        w.getQueue().add(configA1);
        w.getQueue().add(configA2);

        assertThatThrownBy(() -> w.getQueue().flush()).isInstanceOf(IllegalStateException.class).hasMessage("a");
    }
}
