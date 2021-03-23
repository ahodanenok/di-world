package ahodanenok.di;

import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import org.junit.jupiter.api.Test;

import javax.interceptor.AroundConstruct;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class InterceptorTest {

//    public static class A { }
    public static class WithAroundInvoke {
        public void interceptor() { }
    }

    @Test
    public void shouldRegisterAroundConstructInterceptorsOnRegularObject() throws Exception {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(WithAroundInvoke.class).declareInterceptor(
                WithAroundInvoke.class.getDeclaredMethod("interceptor"),
                AroundConstruct.class.getName()));
        w.getQueue().flush();

        // bindings, types, method - expression (aop like)
//        w.getInterceptorChain()

        InterceptorChain chain = w.getInterceptorChain(InterceptorRequest.ofType(AroundConstruct.class.getName()));
        assertThat(chain).isNotNull();

        List<Method> methods = chain.getMethods();
        assertThat(methods).isNotNull().hasSize(1)
                .element(0).isEqualTo(WithAroundInvoke.class.getDeclaredMethod("interceptor"));
    }
}
