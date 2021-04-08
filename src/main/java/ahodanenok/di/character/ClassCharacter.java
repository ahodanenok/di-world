package ahodanenok.di.character;

import ahodanenok.di.exception.ConfigException;
import ahodanenok.di.metadata.ClassMetadataReader;
import ahodanenok.di.metadata.ExecutableMetadataReader;
import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.scope.SingletonScope;

import javax.inject.Singleton;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

// todo: validation
public class ClassCharacter<T> {

    public static <T> ClassCharacter<T> of(Class<T> clazz) {

        if (clazz.isInterface()) {
            throw new ConfigException("Can't instantiate an interface");
        }

        if (Modifier.isAbstract(clazz.getModifiers())) {
            throw new ConfigException("Can't instantiate an interface");
        }

        return new ClassCharacter<T>(clazz);
    }

    private final ClassMetadataReader<T> classMetadataReader;

    private final Class<T> objectClass;
    private Set<String> names;
    private Scope<T> scope;
    private ExecutableMetadataReader constructor;
    private List<Annotation> qualifiers;

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

        this.qualifiers = Collections.unmodifiableList(classMetadataReader.readQualifiers());

        this.interceptor = classMetadataReader.readInterceptor();

        List<Class<?>> interceptors = classMetadataReader.readInterceptors();
        if (interceptors != null) {
            this.interceptors = new ArrayList<>(interceptors);
        } else {
            this.interceptors = Collections.emptyList();
        }

        List<ExecutableMetadataReader> constructors = new ArrayList<>();
        for (Constructor<?> constructor : objectClass.getDeclaredConstructors()) {
            ExecutableMetadataReader metadataReader = new ExecutableMetadataReader(constructor);
            if (metadataReader.readInjectable()) {
                constructors.add(metadataReader);
            }
        }

        if (constructors.size() == 1) {
            this.constructor = constructors.get(0);
        } else if (constructors.size() > 1) {
            throw new IllegalStateException(String.format(
                    "Only one constructor can be injectable, injectable constructos in '%s' are: %s",
                    objectClass,
                    constructors.stream().map(ExecutableMetadataReader::getExecutable).collect(Collectors.toList())));
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

    /**
     * Use constructor to instantiate class
     */
    public ClassCharacter<T> constructedBy(Constructor<?> constructor) {
        // todo: check constructor
        this.constructor = new ExecutableMetadataReader(constructor);
        return this;
    }

    @SuppressWarnings("unchecked") // reader will have constructor from objectClass
    public Constructor<T> getConstructor() {
        if (constructor != null) {
            return (Constructor<T>) constructor.getExecutable();
        } else {
            return null;
        }
    }

    public ClassCharacter<T> withScope(Scope<T> scope) {
        // todo: check not null
        this.scope = scope;
        return this;
    }

    public Scope<T> getScope() {
        return scope;
    }

    public List<Annotation> getQualifiers() {
        return qualifiers;
    }

    /**
     * Set interceptors for a class
     * If called without any parameters - clears interceptors if any
     */
    public ClassCharacter<T> interceptedBy(Class<?>... interceptors) {
        this.interceptors = new ArrayList<>();
        Collections.addAll(this.interceptors, interceptors);
        return this;
    }

    /**
     * Interceptors for a class
     *
     * @return list of interceptor classes, empty if there is no interceptors
     * @see javax.interceptor.Interceptors
     */
    public List<Class<?>> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    /**
     * Mark a method of a class as an interceptor of a type
     *
     * Expects given method to be declared exactly in the class
     * and either have no parameters or InvocationContext as a single parameter
     *
     * @see #intercepts(String, Method)
     */
    public ClassCharacter<T> intercepts(String type, String methodName) {
        try {
            return intercepts(type, objectClass.getDeclaredMethod(methodName));
        } catch (NoSuchMethodException e) {
            // no-op
        }

        try {
            return intercepts(type, objectClass.getDeclaredMethod(methodName, InvocationContext.class));
        } catch (NoSuchMethodException e) {
            // no-op
        }

        throw new IllegalArgumentException(String.format("Interceptor method '%s' not found in '%s'." +
                " Make sure method is present and accepts either no parameters" +
                " or InvocationContext as a single parameter", methodName, objectClass));
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
     * @throws IllegalArgumentException if method doesn't belong to a class
     * @see javax.interceptor.AroundConstruct
     * @see javax.interceptor.AroundInvoke
     * @see javax.annotation.PostConstruct
     * @see javax.annotation.PreDestroy
     */
    public ClassCharacter<T> intercepts(String type, Method method) {
        if (!method.getDeclaringClass().equals(objectClass)) {
            throw new IllegalArgumentException(
                    String.format("Method '%s' doesn't belong to a class '%s'", method, objectClass));
        }

        if (interceptorMethods == null) {
            interceptorMethods = new HashMap<>();
        }

        interceptorMethods.put(type, method);
        return this;
    }

    /**
     * Get interceptor method of type, returns null if there is no such method in a class
     *
     * @see #intercepts(String, Method)
     * @see javax.interceptor.AroundConstruct
     * @see javax.interceptor.AroundInvoke
     * @see javax.annotation.PostConstruct
     * @see javax.annotation.PreDestroy
     */
    public Method getInterceptorMethod(String type) {
        if (interceptorMethods == null) {
            interceptorMethods = new HashMap<>();
        }

        // if there is an entry with null method,
        // then there is no interceptor for this type
        if (interceptorMethods.containsKey(type)) {
            return interceptorMethods.get(type);
        }

        // lazily read interceptor, as we don't know what types
        // we are looking for until they are requested
        return interceptorMethods.computeIfAbsent(type, classMetadataReader::readInterceptorMethod);
    }

    /**
     * Mark class as an interceptor
     *
     * Interceptor classes are handled differently than regular classes,
     * i.e treatment of interceptor methods in a class
     * todo: where to describe differences?
     *
     * @see javax.interceptor.Interceptor
     */
    public ClassCharacter<T> interceptor() {
        this.interceptor = true;
        return this;
    }

    /**
     * Returns true if this class is an interceptor
     */
    public boolean isInterceptor() {
        return interceptor;
    }
}
