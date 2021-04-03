package ahodanenok.di;

import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.SingletonScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ScopeTest {

    public static class A { }

    @Test
    public void shouldUseAlwaysNewScopeIfNoneProvided() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(A.class));
        w.getQueue().flush();

        A first = w.find(ObjectRequest.byType(A.class));
        assertThat(first).isNotNull().isExactlyInstanceOf(A.class);

        A second = w.find(ObjectRequest.byType(A.class));
        assertThat(second).isNotNull().isExactlyInstanceOf(A.class);

        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void shouldReturnNewInstanceOnEveryRequest() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(A.class).withScope(AlwaysNewScope.getInstance()));
        w.getQueue().flush();

        A first = w.find(ObjectRequest.byType(A.class));
        assertThat(first).isNotNull().isExactlyInstanceOf(A.class);

        A second = w.find(ObjectRequest.byType(A.class));
        assertThat(second).isNotNull().isExactlyInstanceOf(A.class);

        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void shouldReturnTheSameInstanceOnEveryRequest() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(A.class).withScope(new SingletonScope<>()));
        w.getQueue().flush();

        A first = w.find(ObjectRequest.byType(A.class));
        assertThat(first).isNotNull().isExactlyInstanceOf(A.class);

        A second = w.find(ObjectRequest.byType(A.class));
        assertThat(second).isNotNull().isExactlyInstanceOf(A.class);

        assertThat(first).isSameAs(second);
    }
}
