package ahodanenok.di.interceptor.context;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodInvocationContextTest {

    private static class A {
        String method(int n, boolean b) {
            return n + " " + b;
        }
    }

    @Test
    public void shouldContainCorrectInfo() throws Exception {
        A obj = new A();
        Method m = A.class.getDeclaredMethod("method", int.class, boolean.class);
        MethodInvocationContext context = new MethodInvocationContext(obj, m);
        context.setParameters(new Object[] { 10, true });

        assertThat(context.getTarget()).isSameAs(obj);
        assertThat(context.getMethod()).isEqualTo(m);
        assertThat(context.getConstructor()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getContextData()).isEmpty();
        assertThat(context.getParameters()).containsExactly(10, true);
    }

    @Test
    public void shouldReturnValueFromMethod() throws Exception {
        Method m = A.class.getDeclaredMethod("method", int.class, boolean.class);
        MethodInvocationContext context = new MethodInvocationContext(new A(), m);
        context.setParameters(new Object[] { 123, true });

        assertThat(context.proceed()).isEqualTo("123 true");
    }

    @Test
    public void shouldChangeNothingAfterProceedInvoked() throws Exception {
        A obj = new A();
        Method m = A.class.getDeclaredMethod("method", int.class, boolean.class);
        MethodInvocationContext context = new MethodInvocationContext(obj, m);
        context.setParameters(new Object[] { 1, false });
        context.proceed();

        assertThat(context.getTarget()).isSameAs(obj);
        assertThat(context.getMethod()).isEqualTo(m);
        assertThat(context.getConstructor()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getContextData()).isEmpty();
        assertThat(context.getParameters()).containsExactly(1, false);
    }

    @Test
    public void shouldChangeParameters() throws Exception {
        Method m = A.class.getDeclaredMethod("method", int.class, boolean.class);
        MethodInvocationContext context = new MethodInvocationContext(new A(), m);
        context.setParameters(new Object[] { 1, false });
        assertThat(context.getParameters()).containsExactly(1, false);

        context.setParameters(new Object[] { 2, true });
        assertThat(context.getParameters()).containsExactly(2, true);

        context.setParameters(new Object[] { 3, false });
        assertThat(context.getParameters()).containsExactly(3, false);
    }

    @Test
    public void shouldBeWritableContextData() throws Exception {
        Method m = A.class.getDeclaredMethod("method", int.class, boolean.class);
        MethodInvocationContext context = new MethodInvocationContext(new A(), m);

        context.getContextData().put("test", 1);
        assertThat(context.getContextData()).hasSize(1);
        assertThat(context.getContextData().get("test")).isEqualTo(1);

        context.getContextData().put("another", true);
        assertThat(context.getContextData()).hasSize(2);
        assertThat(context.getContextData().get("another")).isEqualTo(true);
    }
}
