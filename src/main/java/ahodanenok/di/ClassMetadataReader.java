package ahodanenok.di;

import ahodanenok.di.scope.Scope;

import javax.inject.Named;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassMetadataReader<T> {

    private final Class<T> clazz;

    public ClassMetadataReader(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Reads @Named annotation on a clazz and returns:
     * - non-empty string with an explicit name
     * - empty string meaning default name should be given
     * - null meaning class doesn't have a name
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

    public Scope<T> readScope() {


        return null;
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
