package ahodanenok.di.util;

import ahodanenok.di.util.classes.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class GetMethodsTest {

    @Test
    public void shouldReturnMethodsWithAllModifiers() throws Exception {
        Collection<Method> methods = ReflectionUtils.getInstanceMethods(MethodModifiers.class);
        assertThat(methods).hasSize(4).containsExactlyInAnyOrder(
                MethodModifiers.class.getDeclaredMethod("publicMethod"),
                MethodModifiers.class.getDeclaredMethod("protectedMethod"),
                MethodModifiers.class.getDeclaredMethod("privateMethod"),
                MethodModifiers.class.getDeclaredMethod("packageMethod"));
    }

    static class EmptyClass extends MethodModifiers { }

    @Test
    public void shouldReturnMethodsWithAllModifiersFromSuperclass() throws Exception {
        Collection<Method> methods = ReflectionUtils.getInstanceMethods(EmptyClass.class);
        assertThat(methods).hasSize(4).containsExactlyInAnyOrder(
                MethodModifiers.class.getDeclaredMethod("publicMethod"),
                MethodModifiers.class.getDeclaredMethod("protectedMethod"),
                MethodModifiers.class.getDeclaredMethod("privateMethod"),
                MethodModifiers.class.getDeclaredMethod("packageMethod"));
    }

    @Test
    public void shouldNotReturnConstructors() {
        assertThat(ReflectionUtils.getInstanceMethods(ConstructorModifiers.class)).isEmpty();
    }

    @Test
    public void shouldReturnOverriddenMethodsOnce() throws Exception {
        Collection<Method> methods = ReflectionUtils.getInstanceMethods(Overrides.class);
        assertThat(methods).hasSize(5);

        Map<Class<?>, List<Method>> methodsByClass =
                methods.stream().collect(Collectors.groupingBy(Method::getDeclaringClass));
        assertThat(methodsByClass.get(MethodModifiers.class)).hasSize(1)
                .element(0).isEqualTo(MethodModifiers.class.getDeclaredMethod("privateMethod"));
        assertThat(methodsByClass.get(Overrides.class)).hasSize(4)
                .containsExactlyInAnyOrder(
                        Overrides.class.getDeclaredMethod("publicMethod"),
                        Overrides.class.getDeclaredMethod("protectedMethod"),
                        Overrides.class.getDeclaredMethod("privateMethod"),
                        Overrides.class.getDeclaredMethod("packageMethod"));

    }

    static class NotOverriddenPackageMethod extends PackagePrivateMethod {
        String packageMethod(int a) {
            return "";
        }
    }

    @Test
    public void shouldReturnBothPackagePrivateMethodsIfNotOverridden() throws Exception {
        Collection<Method> methods = ReflectionUtils.getInstanceMethods(NotOverriddenPackageMethod.class);
        assertThat(methods).hasSize(2).containsExactlyInAnyOrder(
                NotOverriddenPackageMethod.class.getDeclaredMethod("packageMethod", int.class),
                PackagePrivateMethod.class.getDeclaredMethod("packageMethod", int.class));
    }

    @Test
    public void shouldNotReturnStaticMethods() {
        assertThat(ReflectionUtils.getInstanceMethods(Statics.class)).isEmpty();
    }
}
