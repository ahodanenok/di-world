package ahodanenok.di;

import ahodanenok.di.character.ClassCharacter;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.*;

public class ConstructorsTest {

    static class A { }
    static class B { }

    static class OneAnnotated {
        A a;
        B b;
        @Inject public OneAnnotated(A a) { this.a = a; }
        public OneAnnotated(B b) { this.b = b; }
    }

    static class SingleDependency {
        A a;
        @Inject public SingleDependency(A a) { this.a = a; }
    }

    static class MultipleDependencies {
        A a;
        B b;
        @Inject public MultipleDependencies(A a, B b) { this.a = a; this.b = b; }
    }

    @Test
    public void shouldFindConstructorWithNoArguments() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(A.class));
        w.getQueue().flush();

        assertThat(w.find(ObjectRequest.byType(A.class))).isNotNull().isExactlyInstanceOf(A.class);
    }

    @Test
    public void shouldFindInjectAnnotatedConstructor() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(OneAnnotated.class));
        w.getQueue().add(ClassCharacter.of(A.class));
        w.getQueue().add(ClassCharacter.of(B.class));
        w.getQueue().flush();

        ObjectAssert<?> a = assertThat(w.find(ObjectRequest.byType(OneAnnotated.class)));
        a.isNotNull().isExactlyInstanceOf(OneAnnotated.class);
        a.extracting("a").isNotNull().isExactlyInstanceOf(A.class);
        a.extracting("b").isNull();
    }

    @Test
    public void shouldFindPublicInjectConstructorAndProvideDependency() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(A.class));
        w.getQueue().add(ClassCharacter.of(SingleDependency.class));
        w.getQueue().flush();

        assertThat(w.find(ObjectRequest.byType(SingleDependency.class)))
                .isNotNull().isExactlyInstanceOf(SingleDependency.class)
                .extracting("a").isNotNull().isExactlyInstanceOf(A.class);
    }

    @Test
    public void shouldFindPublicInjectConstructorAndProvideMultipleDependencies() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(A.class));
        w.getQueue().add(ClassCharacter.of(MultipleDependencies.class));
        w.getQueue().add(ClassCharacter.of(B.class));
        w.getQueue().flush();

        ObjectAssert<?> a = assertThat(w.find(ObjectRequest.byType(MultipleDependencies.class)));
        a.isNotNull().isExactlyInstanceOf(MultipleDependencies.class);
        a.extracting("a").isNotNull().isExactlyInstanceOf(A.class);
        a.extracting("b").isNotNull().isExactlyInstanceOf(B.class);
    }
}
