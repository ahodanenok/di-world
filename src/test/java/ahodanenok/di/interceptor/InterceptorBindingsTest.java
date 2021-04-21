package ahodanenok.di.interceptor;

import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.character.InterceptorCharacter;
import ahodanenok.di.interceptor.context.ObjectInvocationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class InterceptorBindingsTest {

    private static List<String> log = new ArrayList<>();

    @BeforeEach
    public void beforeEach() {
        log = new ArrayList<>();
    }

    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Binding_1 { }

    @Binding_1
    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Binding_2 { }

    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Binding_3 { }

    @Binding_1
    private static class Interceptor_1 {

        public Interceptor_1() { }

        @AroundConstruct
        void aroundConstruct(InvocationContext context) throws Exception {
            log.add("1");
            context.proceed();
        }

        @PostConstruct
        void postConstruct(InvocationContext context) throws Exception {
            log.add("pc1");
            context.proceed();
        }
    }

    @Binding_2
    private static class Interceptor_2 {

        public Interceptor_2() { }

        @AroundConstruct
        void aroundConstruct(InvocationContext context) throws Exception {
            log.add("2");
            context.proceed();
        }
    }

    @Binding_1
    private static class Interceptor_3 {

        public Interceptor_3() { }

        @AroundConstruct
        void aroundConstruct(InvocationContext context) throws Exception {
            log.add("3");
            context.proceed();
        }

        @PostConstruct
        void postConstruct(InvocationContext context) throws Exception {
            log.add("pc3");
            context.proceed();
        }
    }

    @Binding_1
    private static class A {

        @Binding_3
        @Inject A() { }
    }

    private static class B {

        boolean initialized;

        public B() { }

        @Binding_1
        @PostConstruct
        void init() {
            initialized = true;
        }
    }

    @Test
    @DisplayName("should return interceptor chain with interceptor with single binding")
    @Binding_1
    public void shouldGetInterceptorChainSingleBinding() throws Exception {
        World w = new World();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_2.class));
        w.getQueue().flush();

        Annotation b = InterceptorBindingsTest.class
                .getDeclaredMethod("shouldGetInterceptorChainSingleBinding")
                .getDeclaredAnnotation(Binding_1.class);
        InterceptorChain chain = w.getInterceptorChain(
                InterceptorRequest.of(InterceptorType.AROUND_CONSTRUCT).withBindings(Collections.singletonList(b)));

        assertThat(chain.getInterceptors()).hasSize(1);

        assertThat(log).isEmpty();
        chain.invoke(new ObjectInvocationContext("test"));
        assertThat(log).containsExactly("1");
    }

    @Test
    @DisplayName("should return interceptor chain with interceptor with multiple bindings")
    @Binding_1
    @Binding_2
    public void shouldGetInterceptorChainMultipleBindings() throws Exception {
        World w = new World();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_2.class));
        w.getQueue().flush();

        Annotation b1 = InterceptorBindingsTest.class
                .getDeclaredMethod("shouldGetInterceptorChainMultipleBindings")
                .getDeclaredAnnotation(Binding_1.class);
        Annotation b2 = InterceptorBindingsTest.class
                .getDeclaredMethod("shouldGetInterceptorChainMultipleBindings")
                .getDeclaredAnnotation(Binding_2.class);
        InterceptorChain chain = w.getInterceptorChain(
                InterceptorRequest.of(InterceptorType.AROUND_CONSTRUCT).withBindings(Arrays.asList(b1, b2)));

        assertThat(chain.getInterceptors()).hasSize(1);

        assertThat(log).isEmpty();
        chain.invoke(new ObjectInvocationContext("test"));
        assertThat(log).containsExactly("2");
    }

    @Test
    @DisplayName("should call interceptors bound by bindings")
    @Binding_1
    public void shouldInterceptByBindings() {
        World w = new World();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_2.class));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_3.class));
        w.getQueue().add(ClassCharacter.of(B.class));
        w.getQueue().flush();

        assertThat(log).isEmpty();
        B obj = w.find(ObjectRequest.of(B.class));
        assertThat(obj.initialized).isTrue();
        assertThat(log).containsExactlyInAnyOrder("pc1", "pc3");
    }
}
