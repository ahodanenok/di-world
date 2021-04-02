package ahodanenok.di;

import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Named;
import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class ClassMetadataReader<T> {

    private final Class<T> clazz;

    public ClassMetadataReader(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Finds @Named annotation on a clazz
     *
     * @return
     * - non-empty string with an explicit name <br>
     * - empty string meaning default name should be given <br>
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
        }

        return name;
    }

    /**
     * Finds annotation marked with @Scope
     *
     * @return name of scope annotation
     * @throws ConfigException if class contains multiple scope declarations or scope has attributes
     */
    public String readScope() {
        List<Annotation> scopes = ReflectionUtils.getAnnotationsWithMetaAnnotation(clazz, Scope.class);
        if (scopes.size() == 1) {
            Class<? extends Annotation> scope = scopes.get(0).annotationType();
            if (scope.getDeclaredMethods().length > 0) {
                throw new ConfigException(String.format("Scope annotation must not declare any attributes, but '%s' has %s",
                        scope.getName(),
                        Arrays.toString(scope.getDeclaredMethods())));
            }

            return scope.getName();
        } else if (scopes.size() > 1) {
            throw new ConfigException(String.format("Multiple scopes are defined on a class '%s'", clazz));
        } else{
            return null;
        }
    }

    public boolean readInterceptor() {
        return false;
    }

    public Constructor<T> readConstructor() {
        return null;
    }

    public List<Method> readInjectableFields() {
        return new ArrayList<>();
    }

    public List<Method> readInjectableMethods() {
        return new ArrayList<>();
    }

    public List<Class<?>> readInterceptors() {
        return new ArrayList<>();
    }

    public Map<String, Method> readInterceptorMethods() {
        return new HashMap<>();
    }
}
