package ahodanenok.di.interceptor;

import ahodanenok.di.AroundInject;
import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.character.InterceptorCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class AroundInjectInterceptorTest {

    @Interceptor
    private static class Interceptor_1 {

        public Interceptor_1() { }

        @AroundInject
        private Object interceptor(InvocationContext context) throws Exception {
            B b = (B) context.proceed();
            b.log.add("1");
            return b;
        }
    }

    @Interceptor
    private static class Interceptor_2 {

        public Interceptor_2() { }

        @AroundInject
        private Object interceptor(InvocationContext context) throws Exception {
            B b = (B) context.proceed();
            b.log.add("2");
            return b;
        }
    }

    @Interceptor
    private static class Interceptor_3 {

        public Interceptor_3() { }

        @AroundInject
        private Object interceptor(InvocationContext context) throws Exception {
            B b = (B) context.proceed();
            b.log.add("3");
            return b;
        }
    }

    private static class A {

        B b;

        @Inject
        public A(B b) {
            this.b = b;
        }
    }

    private static class B {

        public List<String> log = new ArrayList<>();

        public B() { }
    }


    @Test
    @DisplayName("should call AroundInject interceptors")
    public void interceptors() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(A.class));
        w.getQueue().add(ClassCharacter.of(B.class));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_1.class));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_2.class));
        w.getQueue().add(InterceptorCharacter.of(Interceptor_3.class));
        w.getQueue().flush();

        A a = w.find(ObjectRequest.of(A.class));
        assertThat(a.b).isNotNull();
        assertThat(a.b.log).containsExactlyInAnyOrder("1", "2", "3");
    }
}
