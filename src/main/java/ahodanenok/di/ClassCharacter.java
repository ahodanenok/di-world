package ahodanenok.di;

import ahodanenok.di.metadata.ClassMetadataReader;
import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.scope.SingletonScope;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.*;

// todo: validation
public class ClassCharacter<T> {

    public static <T> ClassCharacter<T> of(Class<T> clazz) {
        // todo: read configuration from annotations
        return new ClassCharacter<T>(clazz);
    }

    private final ClassMetadataReader<T> classMetadataReader;

    private final Class<T> objectClass;
    private Set<String> names;
    private Scope<T> scope;

    private boolean interceptor;
    private List<Class<?>> interceptors;
    private Map<String, Method> interceptorMethods;

    public ClassCharacter(Class<T> clazz) {
        this.classMetadataReader = new ClassMetadataReader<>(clazz);
        this.objectClass = clazz;

        String name = classMetadataReader.readName();
        // todo: default name
        if (name != null) {
            this.names = Collections.singleton(name);
        } else {
            this.names = Collections.emptySet();
        }

        // todo: where to put mapping scopeName -> scope?
        String s = classMetadataReader.readScope();
        if (Singleton.class.getName().equals(s)) {
            this.scope = new SingletonScope<>();
        } else {
            this.scope = AlwaysNewScope.getInstance();
        }

        this.interceptor = classMetadataReader.readInterceptor();

        List<Class<?>> interceptors = classMetadataReader.readInterceptors();
        if (interceptors != null) {
            this.interceptors = new ArrayList<>(interceptors);
        }
    }

    public Class<T> getObjectClass() {
        return objectClass;
    }

    public ClassCharacter<T> knownAs(String... name) {
        if (name.length == 0) {
            return this;
        }

        // todo: validation?
        this.names = new HashSet<>();
        Collections.addAll(this.names, name);
        return this;
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(names);
    }

    public ClassCharacter<T> withScope(Scope<T> scope) {
        // todo: check not null
        this.scope = scope;
        return this;
    }

    public Scope<T> getScope() {
        return scope;
    }

    public ClassCharacter<T> interceptedBy(Class<?>... interceptorClasses) {
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

    public ClassCharacter<T> intercepts(String type, Method method) {
        if (interceptorMethods == null) {
            interceptorMethods = new HashMap<>();
        }

        interceptorMethods.put(type, method);
        return this;
    }

    public Method getInterceptorMethod(String type) {
        if (interceptorMethods == null) {
            return null;
        }

        // if there is entry with null method,
        // then there is no interceptor for this type
        if (interceptorMethods.containsKey(type)) {
            return interceptorMethods.get(type);
        }

        return interceptorMethods.computeIfAbsent(type, classMetadataReader::readInterceptorMethod);
    }

    public ClassCharacter<T> interceptor() {
        this.interceptor = true;
        return this;
    }

    public boolean isInterceptor() {
        return interceptor;
    }
}
