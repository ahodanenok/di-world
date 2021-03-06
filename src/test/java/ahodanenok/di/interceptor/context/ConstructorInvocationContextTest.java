package ahodanenok.di.interceptor.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ConstructorInvocationContextTest {

    private static class NoArgsConstructor { }

    private static class OneArgConstructor {
        int n;
        OneArgConstructor(int n) { this.n = n; }
    }

    private static class MultipleArgsConstructor {
        String a;
        Boolean b;
        long n;
        MultipleArgsConstructor(String a, Boolean b, long n) { this.a = a; this.b = b; this.n = n; }
    }

    @Test
    public void shouldContainCorrectInfoForOneArgConstructor() throws Exception {
        ConstructorInvocationContext context =
                new ConstructorInvocationContext(NoArgsConstructor.class.getDeclaredConstructor());
        assertThat(context.getTarget()).isNull();
        assertThat(context.getMethod()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getConstructor()).isNotNull().isEqualTo(NoArgsConstructor.class.getDeclaredConstructor());
        assertThat(context.getParameters()).isEmpty();
        assertThat(context.getContextData()).isEmpty();
    }

    @Test
    public void shouldContainCorrectInfoForTwoArgConstructor() throws Exception {
        ConstructorInvocationContext context =
                new ConstructorInvocationContext(OneArgConstructor.class.getDeclaredConstructor(int.class));
        assertThat(context.getTarget()).isNull();
        assertThat(context.getMethod()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getConstructor()).isNotNull().isEqualTo(OneArgConstructor.class.getDeclaredConstructor(int.class));
        assertThat(context.getParameters()).hasSize(1).containsOnlyNulls();
        assertThat(context.getContextData()).isEmpty();
    }

    @Test
    public void shouldContainCorrectInfoForMultipleArgsConstructor() throws Exception {
        ConstructorInvocationContext context = new ConstructorInvocationContext(
                        MultipleArgsConstructor.class.getDeclaredConstructor(String.class, Boolean.class, long.class));
        assertThat(context.getTarget()).isNull();
        assertThat(context.getMethod()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getConstructor()).isNotNull().isEqualTo(
                MultipleArgsConstructor.class.getDeclaredConstructor(String.class, Boolean.class, long.class));
        assertThat(context.getParameters()).hasSize(3).containsOnlyNulls();
        assertThat(context.getContextData()).isEmpty();
    }

    @Test
    public void shouldChangeParameters() throws Exception {
        ConstructorInvocationContext context = new ConstructorInvocationContext(
                MultipleArgsConstructor.class.getDeclaredConstructor(String.class, Boolean.class, long.class));
        assertThat(context.getParameters()).hasSize(3).containsOnlyNulls();

        context.setParameters(new Object[] { "first", true, 100L });
        assertThat(context.getParameters()).hasSize(3).containsExactly("first", true, 100L);

        context.setParameters(new Object[] { "second", false, 200L });
        assertThat(context.getParameters()).hasSize(3).containsExactly("second", false, 200L);
    }

    @Test
    public void shouldReturnTheSameObjectFromProceedAndGetTarget() throws Exception {
        ConstructorInvocationContext context =
                new ConstructorInvocationContext(NoArgsConstructor.class.getDeclaredConstructor());

        Object obj = context.proceed();
        assertThat(obj).isNotNull();
        assertThat(context.getTarget()).isNotNull().isSameAs(obj);
        assertThat(context.proceed()).isSameAs(obj);
        assertThat(context.getTarget()).isNotNull().isSameAs(obj);
    }

    @Test
    public void shouldInstantiateOneArgConstructor() throws Exception {
        ConstructorInvocationContext context =
                new ConstructorInvocationContext(OneArgConstructor.class.getDeclaredConstructor(int.class));
        context.setParameters(new Object[] { 10 });

        Object obj = context.proceed();
        assertThat(obj).isNotNull().extracting("n").isEqualTo(10);
    }

    @Test
    public void shouldInstantiateMultipleArgsConstructor() throws Exception {
        ConstructorInvocationContext context = new ConstructorInvocationContext(
                MultipleArgsConstructor.class.getDeclaredConstructor(String.class, Boolean.class, long.class));
        context.setParameters(new Object[] { "abc", true, 100L });

        Object obj = context.proceed();
        assertThat(obj).isNotNull();
        assertThat(obj).extracting("a").isEqualTo("abc");
        assertThat(obj).extracting("b").isEqualTo(true);
        assertThat(obj).extracting("n").isEqualTo(100L);
    }

    @Test
    public void shouldBeWritableContextData() throws Exception {
        ConstructorInvocationContext context =
                new ConstructorInvocationContext(NoArgsConstructor.class.getDeclaredConstructor());

        context.getContextData().put("test", 1);
        assertThat(context.getContextData()).hasSize(1);
        assertThat(context.getContextData().get("test")).isEqualTo(1);

        context.getContextData().put("another", true);
        assertThat(context.getContextData()).hasSize(2);
        assertThat(context.getContextData().get("another")).isEqualTo(true);
    }

    @Test
    public void shouldThrowErrorWhenParameterCountDoesNotMatch() throws Exception {
        ConstructorInvocationContext context =
                new ConstructorInvocationContext(OneArgConstructor.class.getDeclaredConstructor(int.class));

        assertThatThrownBy(() -> context.setParameters(new Object[0]))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Parameters count doesn't match");

        assertThatThrownBy(() -> context.setParameters(new Object[] { 10, 10 }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Parameters count doesn't match");
    }

    @Test
    public void shouldThrowErrorWhenParameterTypeDoesNotMatch() throws Exception {
        ConstructorInvocationContext context =
                new ConstructorInvocationContext(OneArgConstructor.class.getDeclaredConstructor(int.class));

        assertThatThrownBy(() -> context.setParameters(new Object[] { "test" }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Parameter type doesn't match");

        assertThatThrownBy(() -> context.setParameters(new Object[] { 1.0 }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Parameter type doesn't match");
    }

    @Test
    public void shouldThrowErrorWhenNullPassedForPrimitive() throws Exception {
        ConstructorInvocationContext context =
                new ConstructorInvocationContext(OneArgConstructor.class.getDeclaredConstructor(int.class));

        assertThatThrownBy(() -> context.setParameters(new Object[] { null }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Passed null for primitive parameter");
    }
}
