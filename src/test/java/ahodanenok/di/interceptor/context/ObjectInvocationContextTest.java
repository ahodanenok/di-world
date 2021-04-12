package ahodanenok.di.interceptor.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ObjectInvocationContextTest {

    private static class A { }

    @Test
    public void shouldContainCorrectInfo() {
        A obj = new A();
        ObjectInvocationContext context = new ObjectInvocationContext(obj);
        assertThat(context.getTarget()).isSameAs(obj);
        assertThat(context.getMethod()).isNull();
        assertThat(context.getConstructor()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getContextData()).isEmpty();
        assertThat(context.getParameters()).isEmpty();
    }

    @Test
    public void shouldReturnNullFromProceed() {
        ObjectInvocationContext context = new ObjectInvocationContext(new A());
        assertThat(context.proceed()).isNull();
    }

    @Test
    public void shouldChangeNothingAfterProceedInvoked() {
        A obj = new A();
        ObjectInvocationContext context = new ObjectInvocationContext(obj);
        context.proceed();
        assertThat(context.getTarget()).isSameAs(obj);
        assertThat(context.getMethod()).isNull();
        assertThat(context.getConstructor()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getContextData()).isEmpty();
        assertThat(context.getParameters()).isEmpty();
    }

    @Test
    public void shouldChangeParameters() {
        ObjectInvocationContext context = new ObjectInvocationContext(new A());

        context.setParameters(new Object[] { 1, "string" });
        assertThat(context.getParameters()).containsExactly(1, "string");

        context.setParameters(new Object[] { false, 10 });
        assertThat(context.getParameters()).containsExactly(false, 10);
    }

    @Test
    public void shouldBeWritableContextData() {
        ObjectInvocationContext context = new ObjectInvocationContext(new A());

        context.getContextData().put("test", 1);
        assertThat(context.getContextData()).hasSize(1);
        assertThat(context.getContextData().get("test")).isEqualTo(1);

        context.getContextData().put("another", true);
        assertThat(context.getContextData()).hasSize(2);
        assertThat(context.getContextData().get("another")).isEqualTo(true);
    }
}

