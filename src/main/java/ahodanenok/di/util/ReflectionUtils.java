package ahodanenok.di.util;

import java.lang.reflect.Executable;
import java.util.HashMap;
import java.util.Map;

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
}
