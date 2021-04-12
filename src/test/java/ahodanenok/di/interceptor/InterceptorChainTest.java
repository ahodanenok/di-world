package ahodanenok.di.interceptor;

import ahodanenok.di.interceptor.context.ConstructorInvocationContext;
import org.junit.jupiter.api.Test;

import javax.interceptor.InvocationContext;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

public class InterceptorChainTest {

    private static class A { }

    @Test
    public void shouldHaveNoInterceptorsForEmptyChain() {
        InterceptorChain chain = new InterceptorChain();
        assertThat(chain.length()).isEqualTo(0);
        assertThat(chain.getInterceptors()).isEmpty();
    }

    @Test
    public void shouldProceedThroughEmptyChain() throws Exception {
        InvocationContext ctx = new ConstructorInvocationContext(A.class.getDeclaredConstructor());
        assertThat(new InterceptorChain().invoke(ctx)).isNotNull().isExactlyInstanceOf(A.class);
    }

    @Test
    public void shouldProceedThroughSingleInterceptor() throws Exception {
        Interceptor interceptor = InvocationContext::proceed;
        InterceptorChain chain = new InterceptorChain(Collections.singletonList(interceptor));

        Object result = chain.invoke(new ConstructorInvocationContext(A.class.getDeclaredConstructor()));
        assertThat(result).isNotNull().isExactlyInstanceOf(A.class);
    }

    @Test
    public void shouldProceedThroughMultipleInterceptors() throws Exception {
        Interceptor interceptor = InvocationContext::proceed;
        InterceptorChain chain = new InterceptorChain(Arrays.asList(interceptor, interceptor, interceptor));

        Object result = chain.invoke(new ConstructorInvocationContext(A.class.getDeclaredConstructor()));
        assertThat(result).isNotNull().isExactlyInstanceOf(A.class);
    }

    @Test
    public void shouldNotProceedThroughInterceptorsIfProceedIsNotCalledAndReturnResultFromInterceptor() throws Exception {
        boolean[] called = new boolean[2];
        ConstructorInvocationContext ctx = new ConstructorInvocationContext(A.class.getDeclaredConstructor());

        Interceptor interceptor_1 = c -> {
            called[0] = true;
            return "message from interceptor";
        };

        Interceptor interceptor_2 = c -> {
            called[1] = true;
            return c.proceed();
        };

        Object result = new InterceptorChain(Arrays.asList(interceptor_1, interceptor_2)).invoke(ctx);
        assertThat(called[0]).isTrue();
        assertThat(called[1]).isFalse();
        assertThat(result).isNotNull().isEqualTo("message from interceptor");
        assertThat(ctx.getTarget()).isNull();
    }

    @Test
    public void shouldNotProceedThroughInterceptorsIfExceptionIsThrownInInterceptor() throws Exception {
        boolean[] called = new boolean[2];
        ConstructorInvocationContext ctx = new ConstructorInvocationContext(A.class.getDeclaredConstructor());

        Interceptor interceptor_1 = c -> {
            called[0] = true;
            throw new IllegalStateException("exception from interceptor");
        };

        Interceptor interceptor_2 = c -> {
            called[1] = true;
            return c.proceed();
        };

        assertThatThrownBy(() -> new InterceptorChain(Arrays.asList(interceptor_1, interceptor_2)).invoke(ctx))
            .isInstanceOf(IllegalStateException.class).hasMessage("exception from interceptor");
        assertThat(called[0]).isTrue();
        assertThat(called[1]).isFalse();
        assertThat(ctx.getTarget()).isNull();
    }
}
