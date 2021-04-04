package ahodanenok.di.next.metadata;

import ahodanenok.di.exception.ConfigException;
import ahodanenok.di.metadata.ClassMetadataReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.*;

import static org.assertj.core.api.Assertions.*;

public class ReadClassInterceptorsTest {

    @Interceptors({AccessInterceptor.class, LoggingInterceptor.class})
    private static class User { }

    @Interceptors(LoggingInterceptor.class)
    private static class Admin { }


    private static class AccessInterceptor {

        @AroundConstruct
        public void aroundConstruct() { }

        @AroundInvoke
        void aroundInvoke(InvocationContext context) { }

        @PreDestroy
        protected void preDestroy(InvocationContext context) { }

        @PostConstruct
        private void postConstruct(String msg) { }
    }

    @Interceptor
    private static class LoggingInterceptor {

        @AroundConstruct
        private void aroundConstruct_1(int n) { }

        @AroundConstruct
        private void aroundConstruct_1(InvocationContext context) { }
    }

    @Test
    @DisplayName("should return false as interceptor for non interceptor class")
    public void notInterceptor() {
        assertThat(new ClassMetadataReader<>(AccessInterceptor.class).readInterceptor()).isFalse();
    }

    @Test
    @DisplayName("should return true as interceptor for interceptor class")
    public void interceptor() {
        assertThat(new ClassMetadataReader<>(LoggingInterceptor.class).readInterceptor()).isTrue();
    }

    @Test
    @DisplayName("should return no interceptors for a class without interceptors")
    public void noInterceptors() {
        assertThat(new ClassMetadataReader<>(AccessInterceptor.class).readInterceptors()).isEmpty();
    }

    @Test
    @DisplayName("should return single interceptor for a class")
    public void singleInterceptor() {
        assertThat(new ClassMetadataReader<>(Admin.class).readInterceptors()).containsExactly(LoggingInterceptor.class);
    }

    @Test
    @DisplayName("should return multiple interceptors in order for a class")
    public void multipleInterceptors() {
        assertThat(new ClassMetadataReader<>(User.class).readInterceptors())
                .containsExactly(AccessInterceptor.class, LoggingInterceptor.class);
    }

    @Test
    @DisplayName("should read @PostConstruct interceptor method")
    public void postConstruct() throws Exception {
        assertThat(
                new ClassMetadataReader<>(AccessInterceptor.class)
                        .readInterceptorMethod(PostConstruct.class.getName())
        ).isEqualTo(AccessInterceptor.class.getDeclaredMethod("postConstruct", String.class));
    }

    @Test
    @DisplayName("should read @PreDestroy interceptor method")
    public void preDestroy() throws Exception {
        assertThat(
                new ClassMetadataReader<>(AccessInterceptor.class)
                        .readInterceptorMethod(PreDestroy.class.getName())
        ).isEqualTo(AccessInterceptor.class.getDeclaredMethod("preDestroy", InvocationContext.class));
    }

    @Test
    @DisplayName("should read @AroundInvoke interceptor method")
    public void aroundInvoke() throws Exception {
        assertThat(
                new ClassMetadataReader<>(AccessInterceptor.class)
                        .readInterceptorMethod(AroundInvoke.class.getName())
        ).isEqualTo(AccessInterceptor.class.getDeclaredMethod("aroundInvoke", InvocationContext.class));
    }

    @Test
    @DisplayName("should read @AroundConstruct interceptor method")
    public void aroundConstruct() throws Exception {
        assertThat(
                new ClassMetadataReader<>(AccessInterceptor.class)
                        .readInterceptorMethod(AroundConstruct.class.getName())
        ).isEqualTo(AccessInterceptor.class.getDeclaredMethod("aroundConstruct"));
    }

    @Test
    @DisplayName("should not find interceptor method given class doesn't have it")
    public void noInterceptorMethod() {
        assertThat(
                new ClassMetadataReader<>(LoggingInterceptor.class)
                        .readInterceptorMethod(PostConstruct.class.getName())
        ).isNull();
    }

    @Test
    @DisplayName("should throw error given there is more than one interceptor method of a type in a class")
    @Disabled // todo: implement validation
    public void multipleInterceptorMethods() {
        assertThatThrownBy(() ->
                new ClassMetadataReader<>(LoggingInterceptor.class)
                        .readInterceptorMethod(AroundConstruct.class.getName())
        ).isExactlyInstanceOf(ConfigException.class)
                .hasMessageStartingWith("Multiple interceptor methods of type 'javax.interceptor.AroundConstruct'");
    }
}
