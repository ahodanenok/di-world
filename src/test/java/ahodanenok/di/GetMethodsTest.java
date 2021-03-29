package ahodanenok.di;

import ahodanenok.di.pkg.WithPackageMethod;
import ahodanenok.di.util.ReflectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class GetMethodsTest {

    static class ModifiersMethods {
        public void publicMethod() { }
        protected void protectedMethod() { }
        private void privateMethod() { }
        void packageMethod() { }
    }

    @Test
    public void shouldReturnMethodsWithAllModifiers() throws Exception {
        Collection<Method> methods = ReflectionUtils.getInstanceMethods(ModifiersMethods.class);
        assertThat(methods).hasSize(4).containsExactlyInAnyOrder(
                ModifiersMethods.class.getDeclaredMethod("publicMethod"),
                ModifiersMethods.class.getDeclaredMethod("protectedMethod"),
                ModifiersMethods.class.getDeclaredMethod("privateMethod"),
                ModifiersMethods.class.getDeclaredMethod("packageMethod"));
    }

    static class EmptyClass extends ModifiersMethods { }

    @Test
    public void shouldReturnMethodsWithAllModifiersFromSuperclass() throws Exception {
        Collection<Method> methods = ReflectionUtils.getInstanceMethods(EmptyClass.class);
        assertThat(methods).hasSize(4).containsExactlyInAnyOrder(
                ModifiersMethods.class.getDeclaredMethod("publicMethod"),
                ModifiersMethods.class.getDeclaredMethod("protectedMethod"),
                ModifiersMethods.class.getDeclaredMethod("privateMethod"),
                ModifiersMethods.class.getDeclaredMethod("packageMethod"));
    }

    static class Constructors {
        public Constructors(int a) { }
        Constructors(String a) { }
        private Constructors(boolean a) { }
        protected Constructors() { }
    }

    @Test
    public void shouldNotReturnConstructors() {
        assertThat(ReflectionUtils.getInstanceMethods(Constructors.class)).isEmpty();
    }

    static class Overrides extends ModifiersMethods {
        public void publicMethod() { }
        protected void protectedMethod() { }
        private void privateMethod() { }
        void packageMethod() { }
    }

    @Test
    public void shouldReturnOverriddenMethodsOnce() throws Exception {
        Collection<Method> methods = ReflectionUtils.getInstanceMethods(Overrides.class);
        assertThat(methods).hasSize(5);

        Map<Class<?>, List<Method>> methodsByClass =
                methods.stream().collect(Collectors.groupingBy(Method::getDeclaringClass));
        assertThat(methodsByClass.get(ModifiersMethods.class)).hasSize(1)
                .element(0).isEqualTo(ModifiersMethods.class.getDeclaredMethod("privateMethod"));
        assertThat(methodsByClass.get(Overrides.class)).hasSize(4)
                .containsExactlyInAnyOrder(
                        Overrides.class.getDeclaredMethod("publicMethod"),
                        Overrides.class.getDeclaredMethod("protectedMethod"),
                        Overrides.class.getDeclaredMethod("privateMethod"),
                        Overrides.class.getDeclaredMethod("packageMethod"));

    }

    static class NonOverridePackageMethod extends WithPackageMethod {
        String packageMethod(int a) {
            return "";
        }
    }

    @Test
    public void shouldReturnBothPackagePrivateMethodsIfNotOverridden() throws Exception {
        Collection<Method> methods = ReflectionUtils.getInstanceMethods(NonOverridePackageMethod.class);
        assertThat(methods).hasSize(2).containsExactlyInAnyOrder(
                NonOverridePackageMethod.class.getDeclaredMethod("packageMethod", int.class),
                WithPackageMethod.class.getDeclaredMethod("packageMethod", int.class));
    }

    static class Statics {
        public static void publicMethod() { }
        protected static void protectedMethod() { }
        private static void privateMethod() { }
        static void packageMethod() { }
    }

    @Test
    public void shouldNotReturnStaticMethods() {
        assertThat(ReflectionUtils.getInstanceMethods(Statics.class)).isEmpty();
    }
}
