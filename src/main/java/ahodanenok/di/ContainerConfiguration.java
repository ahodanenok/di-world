package ahodanenok.di;

import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.Scope;

import java.lang.reflect.Method;
import java.util.*;

public class ContainerConfiguration<T> {

    public static <T> ContainerConfiguration<T> ofClass(Class<T> clazz) {
        // todo: read configuration from annotations
        return new ContainerConfiguration<T>(clazz);
    }

    private Class<T> objectClass;
    private Set<String> names = new HashSet<>();
    private Scope<T> scope = AlwaysNewScope.getInstance();

    private boolean interceptor;
    private List<Class<?>> interceptors;
    private Map<String, List<Method>> declaredInterceptors;

    public ContainerConfiguration(Class<T> clazz) {
        this.objectClass = clazz;
    }

    public Class<T> getObjectClass() {
        return objectClass;
    }

    public ContainerConfiguration<T> withNames(String... names) {
        this.names.addAll(Arrays.asList(names));
        return this;
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(names);
    }

    public ContainerConfiguration<T> withScope(Scope<T> scope) {
        // todo: check not null
        this.scope = scope;
        return this;
    }

    public Scope<T> getScope() {
        return scope;
    }

    public ContainerConfiguration<T> interceptedBy(Class<?>... interceptorClasses) {
        if (interceptorClasses.length == 0) {
            return this;
        }

        if (interceptors == null) {
            interceptors = new ArrayList<>();
        }

        interceptors.addAll(Arrays.asList(interceptorClasses));
        return this;
    }

    public List<Class<?>> getInterceptors() {
        return interceptors != null ? interceptors : Collections.emptyList();
    }

    public ContainerConfiguration<T> declareInterceptor(Method method, String type) {
        if (declaredInterceptors == null) {
            declaredInterceptors = new HashMap<>();
        }

        declaredInterceptors.computeIfAbsent(type, __ -> new ArrayList<>()).add(method);
        return this;
    }

    public Map<String, List<Method>> getDeclaredInterceptors() {
        return declaredInterceptors != null ? declaredInterceptors : Collections.emptyMap();
    }

    public ContainerConfiguration<T> interceptor() {
        this.interceptor = true;
        return this;
    }

    public boolean isInterceptor() {
        return interceptor;
    }
}
