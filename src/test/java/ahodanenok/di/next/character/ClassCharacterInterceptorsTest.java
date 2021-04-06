package ahodanenok.di.next.character;

import ahodanenok.di.character.ClassCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

public class ClassCharacterInterceptorsTest {

    @Interceptor
    private static class Trash {

        private void postConstruct() { }

        @PreDestroy
        private String preDestroy(InvocationContext context) throws Exception { return ""; }

        private void aroundInvoke(InvocationContext context) { }
    }

    private static class VeryUsefulThing { }

    @Interceptors(VeryUsefulThing.class)
    private static class Stash { }

    @Test
    @DisplayName("should be interceptor given class annotated with @Interceptor annotation")
    public void interceptorAnnotation() {
        assertThat(new ClassCharacter<>(Trash.class).isInterceptor()).isTrue();
    }

    @Test
    @DisplayName("should not be interceptor given class is not annotated with @Interceptor annotation")
    public void notInterceptor() {
        assertThat(new ClassCharacter<>(Stash.class).isInterceptor()).isFalse();
    }

    @Test
    @DisplayName("should be interceptor given interceptor() method is called")
    public void interceptor() {
        assertThat(new ClassCharacter<>(Stash.class).interceptor().isInterceptor()).isTrue();
    }

    @Test
    @DisplayName("should return interceptors given class annotated with @Interceptors annotation")
    public void interceptorsAnnotation() {
        assertThat(new ClassCharacter<>(Stash.class).getInterceptors()).containsExactly(VeryUsefulThing.class);
    }

    @Test
    @DisplayName("should return interceptors given interceptedBy(...) is called")
    public void interceptedBy() {
        assertThat(new ClassCharacter<>(VeryUsefulThing.class)
                .interceptedBy(Trash.class, Stash.class)
                .getInterceptors()).containsExactly(Trash.class, Stash.class);
    }

    @Test
    @DisplayName("should override @Interceptors annotation given interceptedBy(...) is called")
    public void interceptorsOverride() {
        assertThat(new ClassCharacter<>(Stash.class)
                .interceptedBy(Trash.class)
                .getInterceptors()).containsExactly(Trash.class);
    }

    @Test
    @DisplayName("should return no interceptors given no interceptors are defined for a class")
    public void noInterceptors() {
        assertThat(new ClassCharacter<>(VeryUsefulThing.class).getInterceptors()).isEmpty();
    }

    @Test
    @DisplayName("should return an interceptor method given it is defined by annotation")
    public void interceptorMethodAnnotation() throws Exception {
        assertThat(new ClassCharacter<>(Trash.class).getInterceptorMethod(PreDestroy.class.getName()))
                .isEqualTo(Trash.class.getDeclaredMethod("preDestroy", InvocationContext.class));
    }

    @Test
    @DisplayName("should return an interceptor method given it is defined by method object")
    public void interceptorMethodObject() throws Exception {
        assertThat(new ClassCharacter<>(Trash.class)
                .intercepts(PostConstruct.class.getName(), Trash.class.getDeclaredMethod("postConstruct"))
                .getInterceptorMethod(PostConstruct.class.getName())
        ).isEqualTo(Trash.class.getDeclaredMethod("postConstruct"));
    }

    @Test
    @DisplayName("should return an interceptor method given it is defined by method name")
    public void interceptorMethodName() throws Exception {
        assertThat(new ClassCharacter<>(Trash.class)
                .intercepts(AroundInvoke.class.getName(), "aroundInvoke")
                .getInterceptorMethod(AroundInvoke.class.getName())
        ).isEqualTo(Trash.class.getDeclaredMethod("aroundInvoke", InvocationContext.class));
    }

    @Test
    @DisplayName("should return null given interceptor method is not defined")
    public void noInterceptorMethod() {
        assertThat(new ClassCharacter<>(Trash.class).getInterceptorMethod(PostConstruct.class.getName())).isNull();
    }
}
