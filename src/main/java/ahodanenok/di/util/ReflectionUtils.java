package ahodanenok.di.util;

import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;

public class ReflectionUtils {

    public static final Predicate<Annotation> QUALIFIER_PREDICATE =
            (a) -> a.annotationType().isAnnotationPresent(Qualifier.class) && !(a instanceof Named);

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

    public static List<Annotation> getAnnotations(AnnotatedElement element, Predicate<Annotation> predicate) {
        return getAnnotations(element, predicate, false);
    }

    public static List<Annotation> getAnnotations(AnnotatedElement element,
                                                  Predicate<Annotation> predicate,
                                                  boolean composition) {
        List<Annotation> result = new ArrayList<>();

        LinkedList<Annotation> queue = new LinkedList<>();
        for (Annotation a : element.getAnnotations()) {
            if (predicate.test(a)) {
                queue.addLast(a);
            }

            Class<? extends Annotation> repeatable = getRepeatableAnnotationClass(a);

            if (repeatable != null) {
                for (Annotation r : element.getAnnotationsByType(repeatable)) {
                    if (predicate.test(r)) {
                        queue.addLast(r);
                    }
                }
            }
        }

        if (composition) {
            while (!queue.isEmpty()) {
                Annotation current = queue.removeFirst();
                result.add(current);

                for (Annotation a : current.annotationType().getDeclaredAnnotations()) {
                    if (predicate.test(a) && !queue.contains(a)) {
                        queue.addLast(a);
                    }
                }
            }
        } else {
            result.addAll(queue);
        }

        return result;
    }

    private static Class<? extends Annotation> getRepeatableAnnotationClass(Annotation annotation) {
        Method value;
        try {
            value = annotation.annotationType().getDeclaredMethod("value");
        } catch (NoSuchMethodException e) {
            return null;
        }

        // repeatable contains has value method which returns array of @Repeatable annotations
        if (value.getReturnType().isArray()
                && value.getReturnType().getComponentType().isAnnotation()
                && value.getReturnType().getComponentType().isAnnotationPresent(Repeatable.class)) {

            @SuppressWarnings("unchecked") // cast is safe - value() returns an array of annotations
            Class<? extends Annotation> repeatable
                    = (Class<? extends Annotation>) value.getReturnType().getComponentType();

            return repeatable;
        }

        return null;
    }

    // A bean is assignable to a given injection point if:
    public static boolean isAssignable(Class<?> fromType, Class<?> toType) {
        // The bean has a bean type that matches the required type.
        if (fromType == toType) {
            return true;
        }

        // The bean type is assignable to the required type
        if (toType.isAssignableFrom(fromType)) {
            return true;
        }

        // Array types are considered to match only if their element types are identical
        if (fromType.isArray() && toType.isArray() && fromType.getComponentType() == toType.getComponentType()) {
            return true;
        }

        // Primitive types are considered to match their corresponding wrapper types in java.lang
        if (PRIMITIVE_WRAPPERS.getOrDefault(fromType, fromType) == PRIMITIVE_WRAPPERS.getOrDefault(toType, toType)) {
            return true;
        }

        // todo: Assignability of raw and parameterized types

        return false;
    }

    public static boolean isInstantiatable(Class<?> clazz) {
        return !clazz.isInterface()
                && !clazz.isArray()
                && !clazz.isEnum()
                && !clazz.isAnnotation()
                && void.class != clazz
                && !clazz.isPrimitive()
                && !Modifier.isAbstract(clazz.getModifiers());
    }

    public static Object invoke(Method method, Object instance, Object... args) throws InvocationTargetException {
        // todo: make accessible only when needed
        method.setAccessible(true);

        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            // todo: can it happen?
            throw new IllegalStateException(e);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor, Object... parameters) throws InvocationTargetException {
        // todo: make accessible only when needed
        constructor.setAccessible(true);

        try {
            return constructor.newInstance(parameters);
        } catch (IllegalAccessException e) {
            // todo: can it happen?
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            // todo: how to handle?
            throw new RuntimeException(e);
        }
    }

    public static void setField(Field field, Object instance, Object value) {
        // todo: make accessible only when needed
        field.setAccessible(true);

        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            // todo: can it happen?
            e.printStackTrace();
        }
    }
}
