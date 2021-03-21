package ahodanenok.di;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class LookupTest {

    static class A { }
    static class B { }

    static class P { }
    static class C1 extends P { }
    static class C2 extends P { }

    // todo: check with different modifiers

    @Test
    public void shouldReturnObjectByItsType() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(B.class));
        w.getQueue().add(ContainerConfiguration.ofClass(A.class));
        w.getQueue().flush();

        assertThat(w.find(ObjectRequest.byType(A.class))).isNotNull().isExactlyInstanceOf(A.class);
    }

    @Test
    public void shouldReturnObjectByItsParentType() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(C1.class));
        w.getQueue().add(ContainerConfiguration.ofClass(A.class));
        w.getQueue().flush();

        assertThat(w.find(ObjectRequest.byType(P.class))).isNotNull().isExactlyInstanceOf(C1.class);
    }

    @Test
    public void shouldReturnAllByType() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(A.class));
        w.getQueue().add(ContainerConfiguration.ofClass(B.class));
        w.getQueue().add(ContainerConfiguration.ofClass(A.class));
        w.getQueue().flush();

        List<A> values = w.findAll(ObjectRequest.byType(A.class));
        assertThat(values).hasSize(2).hasOnlyElementsOfType(A.class);
    }

    @Test
    public void shouldThrowErrorIfMultipleMatchedWhenOneRequested() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(A.class));
        w.getQueue().add(ContainerConfiguration.ofClass(B.class));
        w.getQueue().add(ContainerConfiguration.ofClass(A.class));
        w.getQueue().flush();

        assertThatThrownBy(() -> w.find(ObjectRequest.byType(A.class)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("multiple");
    }

    @Test
    public void shouldReturnObjectByItsName() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(A.class).withNames("A"));
        w.getQueue().add(ContainerConfiguration.ofClass(B.class).withNames("B"));
        w.getQueue().flush();

        Object value = w.find(ObjectRequest.byName("A"));
        assertThat(value).isNotNull().isExactlyInstanceOf(A.class);
    }

    @Test
    public void shouldReturnObjectByNameIgnoringType() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(A.class).withNames("A"));
        w.getQueue().add(ContainerConfiguration.ofClass(B.class));
        w.getQueue().flush();

        assertThat(w.find(ObjectRequest.byName("A").withType(B.class))).isNotNull().isExactlyInstanceOf(A.class);
    }

    @Test
    public void shouldReturnByTypeQualifiedByName() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(C1.class).withNames("C1"));
        w.getQueue().add(ContainerConfiguration.ofClass(C2.class).withNames("C2"));
        w.getQueue().flush();

        Object value = w.find(ObjectRequest.byName("C1").withType(P.class).qualifyByName());
        assertThat(value).isNotNull().isExactlyInstanceOf(C1.class);
    }
}
