package ahodanenok.di.util;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ReflectionUtils {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new HashMap<>();

    static {
        PRIMITIVE_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPERS.put(short.class, Short.class);
        PRIMITIVE_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVE_WRAPPERS.put(long.class, Long.class);
        PRIMITIVE_WRAPPERS.put(float.class, Float.class);
        PRIMITIVE_WRAPPERS.put(double.class, Double.class);
        PRIMITIVE_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPERS.put(char.class, Character.class);
    }

    public static void validateParameters(Executable executable, Object[] parameters) {
        if (parameters.length != executable.getParameterCount()) {
            throw new IllegalStateException(
                    String.format("Parameters count doesn't match: expected %d, given %d",
                            executable.getParameterCount(), parameters.length));
        }

        Class<?>[] parameterTypes = executable.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            Class<?> expectedType = parameterTypes[i];

            Object param = parameters[i];
            if (param != null && !PRIMITIVE_WRAPPERS.getOrDefault(parameterTypes[i], parameterTypes[i])
                    .isAssignableFrom(PRIMITIVE_WRAPPERS.getOrDefault(param.getClass(), param.getClass()))) {
                throw new IllegalStateException(
                        String.format("Parameter type doesn't match: expecting %s for parameter %d, given %s",
                                expectedType, i, param.getClass()));
            }

            if (param == null && expectedType.isPrimitive()) {
                throw new IllegalStateException(
                        String.format("Passed null for primitive parameter %d of type %s", i, expectedType));
            }

            // todo: this code doesn't let through widening conversions for numbers: int -> long, float -> double, etc, should allow that?
            // todo: think of additional checks
        }
    }

    public static List<Class<?>> getInheritanceChain(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (!currentClass.equals(Object.class)) {
            hierarchy.add(0, currentClass);
            currentClass = currentClass.getSuperclass();
        }

        return hierarchy;
    }

    public static Collection<Method> getInstanceMethods(Class<?> clazz) {
        Set<MethodKey> keys = new HashSet<>();
        List<Method> methods = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            for (Method m : currentClass.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())) {
                    continue;
                }

                // todo: research about bridge and synthetic methods

                if (Modifier.isPrivate(m.getModifiers())) {
                    methods.add(m);
                    continue;
                }

                if (keys.add(new MethodKey(m))) {
                    // todo: specific
                    methods.add(m);
                }
            }

            currentClass = currentClass.getSuperclass();

            if (Object.class.equals(currentClass)) {
                break;
            }
        }

        return methods;
    }

    private static boolean isPackagePrivate(Executable executable) {
        int m = executable.getModifiers();
        return !Modifier.isPrivate(m) && !Modifier.isProtected(m) && !Modifier.isPublic(m);
    }

    private static class MethodKey {

        private final Method method;

        MethodKey(Method method) {
            this.method = method;
        }

        @Override
        public int hashCode() {
            return 31 * 31 * method.getName().hashCode()
                    + 31 * Arrays.hashCode(method.getParameterTypes())
                    + (isPackagePrivate(method) ? 1 : 0);
        }

        @Override
        public boolean equals(Object obj) {
            // never null

            if (obj == this) {
                return true;
            }

            // don't check - obj will always be of type MethodKey
            // todo: suppress warning
            MethodKey other = (MethodKey) obj;

            // names are case-sensitive
            if (!other.method.getName().equals(method.getName())) {
                return false;
            }

            if (!Arrays.equals(other.method.getParameterTypes(), method.getParameterTypes())) {
                return false;
            }

            if (isPackagePrivate(other.method) && isPackagePrivate(method)) {
                return other.method.getDeclaringClass().getPackage().getName()
                        .equals(method.getDeclaringClass().getPackage().getName());
            }

            return true;
        }
    }
}
