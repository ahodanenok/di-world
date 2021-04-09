package ahodanenok.di.metadata;

import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.interceptor.Interceptor;
import javax.interceptor.Interceptors;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ClassMetadataReader<T> {

    private final Class<T> clazz;

    public ClassMetadataReader(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Finds @Named annotation on a clazz
     *
     * @return
     * - non-empty string with a name <br>
     * - null meaning class doesn't have a name <br>
     */
    public String readName() {
        Named named = clazz.getAnnotation(Named.class);

        String name;
        if (named != null) {
            name = named.value();
        } else {
            name = null;
        }

        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                name = Introspector.decapitalize(clazz.getSimpleName());
            }
        }

        return name;
    }

    /**
     * Finds annotation marked with @Scope
     *
     * @return name of scope annotation
     * @throws CharacterMetadataException if class contains multiple scope declarations or scope has attributes
     */
    public String readScope() {
        List<Annotation> scopes = ReflectionUtils.getAnnotations(
                clazz, a -> a.annotationType().isAnnotationPresent(Scope.class));
        if (scopes.size() == 1) {
            Class<? extends Annotation> scope = scopes.get(0).annotationType();
            if (scope.getDeclaredMethods().length > 0) {
                throw new CharacterMetadataException(String.format("Scope annotation must not declare any attributes, but '%s' has %s",
                        scope.getName(),
                        Arrays.toString(scope.getDeclaredMethods())));
            }

            return scope.getName();
        } else if (scopes.size() > 1) {
            throw new CharacterMetadataException(String.format("Multiple scopes are defined on a class '%s'", clazz));
        } else{
            return null;
        }
    }

    public boolean readInterceptor() {
        return clazz.isAnnotationPresent(Interceptor.class);
    }

    public List<Class<?>> readInterceptors() {
        Interceptors interceptors = clazz.getDeclaredAnnotation(Interceptors.class);
        if (interceptors != null) {
            return Arrays.stream((Class<?>[]) interceptors.value()).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public Method readInterceptorMethod(String type) {
        for (Method method : ReflectionUtils.getInstanceMethods(clazz)) {
            for (Annotation a : method.getAnnotations()) {
                if (a.annotationType().getName().equals(type)) {
                    return method;
                }
            }
        }

        return null;
    }

    /**
     * Find all @Qualifier annotations on the class
     */
    public Set<Annotation> readQualifiers() {
        return new HashSet<>(ReflectionUtils.getAnnotations(clazz,
                a -> a.annotationType().isAnnotationPresent(Qualifier.class)));
    }
}
