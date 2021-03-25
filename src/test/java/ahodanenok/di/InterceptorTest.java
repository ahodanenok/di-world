package ahodanenok.di;

import ahodanenok.di.interceptor.Interceptor;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import org.junit.jupiter.api.Test;

import javax.interceptor.AroundConstruct;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class InterceptorTest {

    public static class WithAroundInvoke {
        public void interceptor() { }
    }

    @Test
    public void shouldRegisterAroundConstructInterceptorDeclaredInClass() throws Exception {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(WithAroundInvoke.class).declareInterceptor(
                WithAroundInvoke.class.getDeclaredMethod("interceptor"),
                AroundConstruct.class.getName()));
        w.getQueue().flush();

        InterceptorChain chain = w.getInterceptorChain(InterceptorRequest.ofType(AroundConstruct.class.getName()));
        assertThat(chain).isNotNull();

        List<Interceptor> methods = chain.getInterceptors();
        assertThat(methods).isNotNull().hasSize(1);
    }
}
