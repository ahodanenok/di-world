package ahodanenok.di;

import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.SingletonScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ScopeTest {

    public static class A { }

    @Test
    public void shouldUseAlwaysNewScopeIfNoneProvided() {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(A.class));
        w.getQueue().flush();

        A first = w.find(ObjectRequest.of(A.class));
        assertThat(first).isNotNull().isExactlyInstanceOf(A.class);

        A second = w.find(ObjectRequest.of(A.class));
        assertThat(second).isNotNull().isExactlyInstanceOf(A.class);

        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void shouldReturnNewInstanceOnEveryRequest() {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(A.class).scopedBy(AlwaysNewScope.getInstance()));
        w.getQueue().flush();

        A first = w.find(ObjectRequest.of(A.class));
        assertThat(first).isNotNull().isExactlyInstanceOf(A.class);

        A second = w.find(ObjectRequest.of(A.class));
        assertThat(second).isNotNull().isExactlyInstanceOf(A.class);

        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void shouldReturnTheSameInstanceOnEveryRequest() {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(A.class).scopedBy(new SingletonScope<>()));
        w.getQueue().flush();

        A first = w.find(ObjectRequest.of(A.class));
        assertThat(first).isNotNull().isExactlyInstanceOf(A.class);

        A second = w.find(ObjectRequest.of(A.class));
        assertThat(second).isNotNull().isExactlyInstanceOf(A.class);

        assertThat(first).isSameAs(second);
    }
}
