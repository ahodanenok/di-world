package ahodanenok.di.interceptor;

import ahodanenok.di.DefaultWorld;
import ahodanenok.di.ObjectRequest;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.character.InterceptorCharacter;
import ahodanenok.di.interceptor.context.ObjectInvocationContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.InvocationContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class AroundConstructInterceptorsTest {

    private static List<String> calledInterceptors = new ArrayList<>();

    public static class A { }

    public static class B {
        public Dependency dependency;
        @Inject public B(Dependency dependency) { this.dependency = dependency; }
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
    public void shouldRegisterInterceptor() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                    Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().flush();

        InterceptorChain chain = w.getInterceptorChain(
                InterceptorRequest.of(InterceptorType.AROUND_CONSTRUCT).matchAll());
        assertThat(chain).isNotNull();
        assertThat(chain.getInterceptors()).hasSize(1);

        assertThat(calledInterceptors).isEmpty();
        assertThat(chain.invoke(new ObjectInvocationContext(new A()))).isNull();
        assertThat(calledInterceptors).containsExactly("interceptor 1");
    }

    @Test
    public void shouldRegisterSameMultipleInterceptors() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().flush();

        InterceptorChain chain = w.getInterceptorChain(
                InterceptorRequest.of(InterceptorType.AROUND_CONSTRUCT).matchAll());
        assertThat(chain).isNotNull();
        assertThat(chain.getInterceptors()).hasSize(3);

        assertThat(calledInterceptors).isEmpty();
        assertThat(chain.invoke(new ObjectInvocationContext(new A()))).isNull();
        assertThat(calledInterceptors).containsExactly("interceptor 1", "interceptor 1", "interceptor 1");
    }

    @Test
    public void shouldRegisterMultipleInterceptors() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_2.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_2.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().flush();

        InterceptorChain chain = w.getInterceptorChain(
                InterceptorRequest.of(InterceptorType.AROUND_CONSTRUCT).matchAll());
        assertThat(chain).isNotNull();
        assertThat(chain.getInterceptors()).hasSize(2);

        assertThat(calledInterceptors).isEmpty();
        assertThat(chain.invoke(new ObjectInvocationContext(new A()))).isNull();
        assertThat(calledInterceptors).containsExactly("interceptor 1", "interceptor 2");
    }

    @Test
    public void shouldInvokeInterceptor() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(A.class).interceptedBy(Interceptor_1.class));
        w.getQueue().flush();

        assertThat(calledInterceptors).isEmpty();
        Assertions.assertThat(w.find(ObjectRequest.of(A.class))).isExactlyInstanceOf(A.class);
        assertThat(calledInterceptors).containsExactly("interceptor 1");
    }

    @Test
    public void shouldInvokeOnlyDeclaredInterceptor() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_2.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_2.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(A.class).interceptedBy(Interceptor_2.class));
        w.getQueue().flush();

        assertThat(calledInterceptors).isEmpty();
        assertThat(w.find(ObjectRequest.of(A.class))).isExactlyInstanceOf(A.class);
        assertThat(calledInterceptors).containsExactly("interceptor 2");
    }

    @Test
    public void shouldNotInvokeInterceptorIfNotDefined() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_2.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_2.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(A.class));
        w.getQueue().flush();

        assertThat(calledInterceptors).isEmpty();
        assertThat(w.find(ObjectRequest.of(A.class))).isExactlyInstanceOf(A.class);
        assertThat(calledInterceptors).isEmpty();
    }

    @Test
    public void shouldInvokeDeclaredInterceptorsInOrder() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_2.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_2.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(A.class)
                .interceptedBy(Interceptor_2.class, Interceptor_1.class));
        w.getQueue().flush();

        assertThat(calledInterceptors).isEmpty();
        assertThat(w.find(ObjectRequest.of(A.class))).isExactlyInstanceOf(A.class);
        assertThat(calledInterceptors).containsExactly("interceptor 2", "interceptor 1");
    }

    @Test
    public void shouldChangeConstructorParameterInInterceptor() throws Exception {
        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_1.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_3.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_3.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_2.class)
                .intercepts(
                        InterceptorType.AROUND_CONSTRUCT,
                        Interceptor_2.class.getDeclaredMethod("interceptor", InvocationContext.class)));
        w.getQueue().add(ClassCharacter.of(Dependency.class));
        w.getQueue().add(ClassCharacter.of(B.class)
                .interceptedBy(Interceptor_1.class, Interceptor_3.class, Interceptor_2.class));
        w.getQueue().flush();

        B object = w.find(ObjectRequest.of(B.class));
        assertThat(object.dependency.value).isEqualTo("from interceptor 3");
    }
}
