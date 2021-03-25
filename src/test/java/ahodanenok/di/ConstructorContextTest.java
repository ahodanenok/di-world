package ahodanenok.di;

import ahodanenok.di.interceptor.context.ConstructorContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ConstructorContextTest {

    public static class NoArgsConstructor {
        public NoArgsConstructor() { }
    }

    public static class OneArgConstructor {
        public OneArgConstructor(int n) { }
    }

    public static class MultipleArgsConstructor {
        public String a;
        public Boolean b;
        public long n;
        public MultipleArgsConstructor(String a, Boolean b, long n) { this.a = a; this.b = b; this.n = n; }
    }

    @Test
    public void shouldContainCorrectInfoWhenContextForOneArgConstructor() throws Exception {
        ConstructorContext context = new ConstructorContext(NoArgsConstructor.class.getDeclaredConstructor());
        assertThat(context.getTarget()).isNull();
        assertThat(context.getMethod()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getConstructor()).isNotNull().isEqualTo(NoArgsConstructor.class.getDeclaredConstructor());
        assertThat(context.getParameters()).isEmpty();
        assertThat(context.getContextData()).isEmpty();
    }

    @Test
    public void shouldContainCorrectInfoWhenContextForTwoArgConstructor() throws Exception {
        ConstructorContext context = new ConstructorContext(OneArgConstructor.class.getDeclaredConstructor(int.class));
        assertThat(context.getTarget()).isNull();
        assertThat(context.getMethod()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getConstructor()).isNotNull().isEqualTo(OneArgConstructor.class.getDeclaredConstructor(int.class));
        assertThat(context.getParameters()).hasSize(1).containsOnlyNulls();
        assertThat(context.getContextData()).isEmpty();
    }

    @Test
    public void shouldContainCorrectInfoWhenContextForMultipleArgsConstructor() throws Exception {
        ConstructorContext context = new ConstructorContext(MultipleArgsConstructor.class.getDeclaredConstructor(String.class, Boolean.class, long.class));
        assertThat(context.getTarget()).isNull();
        assertThat(context.getMethod()).isNull();
        assertThat(context.getTimer()).isNull();
        assertThat(context.getConstructor()).isNotNull().isEqualTo(MultipleArgsConstructor.class.getDeclaredConstructor(String.class, Boolean.class, long.class));
        assertThat(context.getParameters()).hasSize(3).containsOnlyNulls();
        assertThat(context.getContextData()).isEmpty();
    }

    @Test
    public void shouldChangeParameters() throws Exception {
        ConstructorContext context = new ConstructorContext(MultipleArgsConstructor.class.getDeclaredConstructor(String.class, Boolean.class, long.class));
        assertThat(context.getParameters()).hasSize(3).containsOnlyNulls();

        context.setParameters(new Object[] { "first", true, 100 });
        assertThat(context.getParameters()).hasSize(3).containsExactly("first", true, 100);

        context.setParameters(new Object[] { "second", false, 200 });
        assertThat(context.getParameters()).hasSize(3).containsExactly("second", false, 200);
    }

    @Test
    public void shouldInstantiateNoArgConstructor() throws Exception {
        ConstructorContext context = new ConstructorContext(NoArgsConstructor.class.getDeclaredConstructor());

        Object obj = context.proceed();
        assertThat(obj).isNotNull();
        assertThat(context.getTarget()).isNotNull().isSameAs(obj);
        assertThat(context.proceed()).isSameAs(obj);
        assertThat(context.getTarget()).isNotNull().isSameAs(obj);
    }
}
