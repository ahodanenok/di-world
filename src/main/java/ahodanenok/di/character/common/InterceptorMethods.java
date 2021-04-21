package ahodanenok.di.character.common;

import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.interceptor.InterceptorType;
import ahodanenok.di.metadata.ClassMetadataReader;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InterceptorMethods<T> {

    private final Class<T> clazz;
    private final ClassMetadataReader<T> metadataReader;
    private Map<InterceptorType, Method> methods;

    public InterceptorMethods(Class<T> clazz) {
        this.clazz = clazz;
        this.metadataReader = new ClassMetadataReader<>(clazz);
    }

    /**
     * Mark a method of a class as an interceptor of a type
     *
     * Expects given method to be declared exactly in the class
     * and either have no parameters or InvocationContext as a single parameter
     *
     * @see #intercepts(InterceptorType, Method)
     */
    public void intercepts(InterceptorType type, String methodName) {
        try {
            intercepts(type, clazz.getDeclaredMethod(methodName));
            return;
        } catch (NoSuchMethodException e) {
            // no-op
        }

        try {
            intercepts(type, clazz.getDeclaredMethod(methodName, InvocationContext.class));
            return;
        } catch (NoSuchMethodException e) {
            // no-op
        }

        throw new CharacterMetadataException(String.format("Interceptor method '%s' not found in '%s'." +
                " Make sure method is present and accepts either no parameters" +
                " or InvocationContext as a single parameter", methodName, clazz));
    }

    /**
     * Mark a method of a class as an interceptor of a type
     * Different types may impose different requirements for interceptor methods
     *
     * For the following interceptors, type is the same as annotation's FQCN:
     * <ul>
     * <li>javax.interceptor.AroundConstruct</li>
     * <li>javax.interceptor.AroundInvoke</li>
     * <li>javax.annotation.PostConstruct</li>
     * <li>javax.annotation.PreDestroy</li>
     * </ul>
     *
     * @param type type of intercepted event
     * @param method method of a class
     * @throws CharacterMetadataException if method doesn't belong to a class
     * @see javax.interceptor.AroundConstruct
     * @see javax.interceptor.AroundInvoke
     * @see javax.annotation.PostConstruct
     * @see javax.annotation.PreDestroy
     */
    public void intercepts(InterceptorType type, Method method) {
        if (type == null) {
            throw new CharacterMetadataException("Type can't be null");
        }

        if (method == null) {
            throw new CharacterMetadataException("Method can't be null");
        }

        if (method.getDeclaringClass() != clazz) {
            throw new CharacterMetadataException(
                    String.format("Method '%s' doesn't belong to a class '%s'", method, clazz));
        }

        // todo: validate method signature

        if (methods == null) {
            methods = new HashMap<>();
        }

        methods.put(type, method);
    }

    /**
     * Get interceptor method of type, returns null if there is no such method in a class
     *
     * @see #intercepts(InterceptorType, Method)
     * @see javax.interceptor.AroundConstruct
     * @see javax.interceptor.AroundInvoke
     * @see javax.annotation.PostConstruct
     * @see javax.annotation.PreDestroy
     */
    public Method get(InterceptorType type) {
        if (methods == null) {
            methods = new HashMap<>();
        }

        // if there is an entry with null method,
        // then there is no interceptor for this type
        if (methods.containsKey(type)) {
            return methods.get(type);
        }

        // lazily read interceptor, as we don't know what types
        // we are looking for until they are requested
        return methods.computeIfAbsent(type, metadataReader::readInterceptorMethod);
    }
}
