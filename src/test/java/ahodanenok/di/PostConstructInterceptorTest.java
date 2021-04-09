package ahodanenok.di;

import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.interceptor.context.MethodInvocationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PostConstructInterceptorTest {

    private static List<String> calledInterceptors = new ArrayList<>();

    public static class A {
        @PostConstruct public void m() {
            calledInterceptors.add("A");
        }
    }
    public static class C {
        public void m() {
            calledInterceptors.add("C");
        }
    }

    public static class B {
        public Dependency dependency;
        @Inject
        public B(Dependency dependency) { this.dependency = dependency; }
    }

    public static class Dependency {
        private String value;
        public Dependency() { this.value = "default"; }
        public Dependency(String value) { this.value = value; }
    }

    public static class Interceptor_1 {
        public Object interceptor(InvocationContext context) throws Exception {
            calledInterceptors.add("interceptor 1");
            return context.proceed();
        }
    }

    public static class Interceptor_2 {
        public Object interceptor(InvocationContext context) throws Exception {
            calledInterceptors.add("interceptor 2");
            return context.proceed();
        }
    }

    public static class Interceptor_3 {
        public Object interceptor(InvocationContext context) throws Exception {
            calledInterceptors.add("interceptor 3");
            context.setParameters(new Object[] { new Dependency("from interceptor 3") });
            return context.proceed();
        }
    }

    @BeforeEach
    public void beforeEach() {
        calledInterceptors = new ArrayList<>();
    }

    @Test
    public void shouldInvokePostConstructInterceptorChain() throws Exception {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Interceptor_1.class)
                .interceptor()
                .intercepts(
                        PostConstruct.class.getName(),
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().flush();

        InterceptorChain chain = w.getInterceptorChain(
                InterceptorRequest.of(PostConstruct.class.getName()).matchAll());
        assertThat(chain).isNotNull();
        assertThat(chain.getInterceptors()).hasSize(1);

        assertThat(calledInterceptors).isEmpty();
        assertThat(chain.invoke(new MethodInvocationContext(new A(), A.class.getDeclaredMethod("m")))).isNull();
        assertThat(calledInterceptors).containsExactly("interceptor 1", "A");
    }

    @Test
    public void shouldRegisterSameMultipleInterceptors() throws Exception {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Interceptor_1.class)
                .interceptor()
                .intercepts(
                        PostConstruct.class.getName(),
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(Interceptor_1.class)
                .interceptor()
                .intercepts(
                        PostConstruct.class.getName(),
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(Interceptor_1.class)
                .interceptor()
                .intercepts(
                        PostConstruct.class.getName(),
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().flush();

        InterceptorChain chain = w.getInterceptorChain(
                InterceptorRequest.of(PostConstruct.class.getName()).matchAll());
        assertThat(chain).isNotNull();
        assertThat(chain.getInterceptors()).hasSize(3);

        assertThat(calledInterceptors).isEmpty();
        assertThat(chain.invoke(new MethodInvocationContext(new A(), A.class.getDeclaredMethod("m")))).isNull();
        assertThat(calledInterceptors).containsExactly("interceptor 1", "interceptor 1", "interceptor 1", "A");
    }

    @Test
    public void shouldInvokeInterceptor() throws Exception {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Interceptor_1.class)
                .interceptor()
                .intercepts(
                        PostConstruct.class.getName(),
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(A.class).interceptedBy(Interceptor_1.class));
        w.getQueue().flush();

        assertThat(calledInterceptors).isEmpty();
        assertThat(w.find(ObjectRequest.of(A.class))).isExactlyInstanceOf(A.class);
        assertThat(calledInterceptors).containsExactly("interceptor 1", "A");
    }

    @Test
    public void shouldInvokeInterceptorWithoutMethodInClass() throws Exception {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Interceptor_1.class)
                .interceptor()
                .intercepts(
                        PostConstruct.class.getName(),
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(C.class).interceptedBy(Interceptor_1.class));
        w.getQueue().flush();

        assertThat(calledInterceptors).isEmpty();
        assertThat(w.find(ObjectRequest.of(C.class))).isExactlyInstanceOf(C.class);
        assertThat(calledInterceptors).containsExactly("interceptor 1");
    }

    @Test
    public void shouldInvokeDeclaredInterceptorsInOrder() throws Exception {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Interceptor_1.class)
                .interceptor()
                .intercepts(
                        PostConstruct.class.getName(),
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(Interceptor_2.class)
                .interceptor()
                .intercepts(
                        PostConstruct.class.getName(),
                        Interceptor_2.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(A.class)
                .interceptedBy(Interceptor_2.class, Interceptor_1.class));
        w.getQueue().flush();

        assertThat(calledInterceptors).isEmpty();
        assertThat(w.find(ObjectRequest.of(A.class))).isExactlyInstanceOf(A.class);
        assertThat(calledInterceptors).containsExactly("interceptor 2", "interceptor 1", "A");
    }
}
