package ahodanenok.di.next.scope;

import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.SingletonScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;

import static org.assertj.core.api.Assertions.*;

public class ScopeTest {

    private static class Box { }

    @Test
    @DisplayName("should return the same object from singleton scope")
    public void singleton() {
        SingletonScope<Box> scope = new SingletonScope<>();
        Provider<Box> provider = Box::new;

        assertThat(scope.getObject(provider))
                .isSameAs(scope.getObject(provider))
                .isSameAs(scope.getObject(provider));
    }

    @Test
    @DisplayName("should return a new object from always new scope")
    public void alwaysNew() {
        AlwaysNewScope<Box> scope = new AlwaysNewScope<>();
        Provider<Box> provider = Box::new;

        assertThat(scope.getObject(provider))
                .isNotSameAs(scope.getObject(provider))
                .isNotSameAs(scope.getObject(provider));
    }
}
