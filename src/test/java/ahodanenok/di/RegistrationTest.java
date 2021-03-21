package ahodanenok.di;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class RegistrationTest {

    public static class A { }

    @Test
    public void shouldRegisterComponentAfterFlush() {
        ContainerConfiguration config = ContainerConfiguration.ofClass(A.class);

        World w = new World();
        assertThat(w).hasSize(0);

        w.getQueue().add(config);
        assertThat(w).hasSize(0);

        w.getQueue().flush();
        assertThat(w).hasSize(1).doesNotContainNull();
    }

    @Test
    public void shouldCreateComponentWithTypeFromConfiguration() {
        ContainerConfiguration config = ContainerConfiguration.ofClass(A.class);

        World w = new World();
        w.getQueue().add(config);
        w.getQueue().flush();

        assertThat(w.iterator().next().getType()).isEqualTo(A.class);
    }


    @Test
    public void shouldCreateComponentWithNamesFromConfiguration() {
        ContainerConfiguration config = ContainerConfiguration.ofClass(A.class).withNames("a", "b", "a", "c");

        World w = new World();
        w.getQueue().add(config);
        w.getQueue().flush();

        assertThat(w.iterator().next().getNames()).containsOnly("a", "b", "c");
    }
}
