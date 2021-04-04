package ahodanenok.di.next.character;

import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.scope.SingletonScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

public class ClassCharacterScopeTest {

    private static class ThreadScope<T> implements Scope<T> {
        @Override
        public T getObject(Provider<T> provider) {
           return null;
        }
    }

    private static class RequestContext { }

    @Singleton
    private static class ApplicationContext { }

    @Test
    @DisplayName("should have AlwaysNew scope given no scope is set")
    public void alwaysNew() {
        assertThat(ClassCharacter.of(RequestContext.class).getScope()).isExactlyInstanceOf(AlwaysNewScope.class);
    }

    @Test
    @DisplayName("should be singleton given singleton set by annotation")
    public void singletonAnnotation() {
        assertThat(ClassCharacter.of(ApplicationContext.class).getScope())
                .isExactlyInstanceOf(SingletonScope.class);
    }

    @Test
    @DisplayName("should be singleton given singleton set explicitly")
    public void singletonExplicit() {
        assertThat(ClassCharacter.of(RequestContext.class).withScope(new SingletonScope<>()).getScope())
                .isExactlyInstanceOf(SingletonScope.class);
    }

    @Test
    @DisplayName("should override scope from annotation")
    public void overrides() {
        assertThat(ClassCharacter.of(ApplicationContext.class).withScope(new ThreadScope<>()).getScope())
                .isExactlyInstanceOf(ThreadScope.class);
    }
}
