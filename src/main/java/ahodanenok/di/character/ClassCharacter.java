package ahodanenok.di.character;

import ahodanenok.di.World;
import ahodanenok.di.container.impl.DefaultClassContainer;
import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.metadata.ClassMetadataReader;
import ahodanenok.di.metadata.ExecutableMetadataReader;
import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.scope.SingletonScope;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Singleton;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

// todo: docs
public class ClassCharacter<T> implements Character<T> {

    public static <T> ClassCharacter<T> of(Class<T> clazz) {
        if (clazz == null) {
            throw new CharacterMetadataException("Class can't be null");
        }

        if (!ReflectionUtils.isInstantiatable(clazz)) {
            throw new CharacterMetadataException(String.format("Class '%s' is not instantiatable", clazz.getName()));
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
    private List<Annotation> interceptorBindings;
    private Map<String, Method> interceptorMethods;

    public ClassCharacter(Class<T> clazz) {
        this.classMetadataReader = new ClassMetadataReader<>(clazz);
        this.objectClass = clazz;

        String name = classMetadataReader.readNamed();
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

        this.qualifiers = classMetadataReader.readQualifiers();

        this.interceptor = classMetadataReader.readInterceptor();
        this.interceptors = classMetadataReader.readInterceptors();
        this.interceptors.forEach(this::validateInterceptor);
        this.interceptorBindings = classMetadataReader.readInterceptorBindings();

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
            throw new CharacterMetadataException(String.format(
                    "Only one constructor can be injectable, injectable constructos in '%s' are: %s",
                    objectClass,
                    constructors.stream().map(ExecutableMetadataReader::getExecutable).collect(Collectors.toList())));
        }
    }

    public Class<T> getObjectClass() {
        return objectClass;
    }

    // todo: doc
    public ClassCharacter<T> knownAs(String... names) {
        return knownAs(Arrays.asList(names));
    }

    public ClassCharacter<T> knownAs(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            throw new CharacterMetadataException("Provide at least one name");
        }

        Set<String> newNames = new HashSet<>();
        for (String name : names) {
            if (name == null) {
                throw new CharacterMetadataException("Name can't be null");
            }

            String n = name.trim();
            if (n.isEmpty()) {
                throw new CharacterMetadataException("Name can't be empty");
            }

            newNames.add(n);
        }

        this.names = newNames;
        return this;
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(names);
    }

    /**
     * Use constructor to instantiate class
     * todo: doc
     */
    public ClassCharacter<T> constructedBy(Constructor<?> constructor) {
        if (constructor == null) {
            throw new CharacterMetadataException("Constructor can't be null");
        }

        if (constructor.getDeclaringClass() != objectClass) {
            throw new CharacterMetadataException(String.format(
                    "Constructor doesn't belong to class '%s'", objectClass));
        }

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

    public ClassCharacter<T> scopedBy(Scope<T> scope) {
        if (scope == null) {
            throw new CharacterMetadataException("Scope can't be null");
        }

        this.scope = scope;
        return this;
    }

    public Scope<T> getScope() {
        return scope;
    }

    public ClassCharacter<T> qualifiedAs(Annotation... qualifiers) {
        return qualifiedAs(Arrays.asList(qualifiers));
    }

    public ClassCharacter<T> qualifiedAs(Collection<Annotation> qualifiers) {
        if (qualifiers == null || qualifiers.isEmpty()) {
            throw new CharacterMetadataException("Provide at least one qualifier");
        }

        List<Annotation> newQualifiers = new ArrayList<>();
        for (Annotation q : qualifiers) {
            if (q == null) {
                throw new CharacterMetadataException("Qualifier can't be null");
            }

            // todo: should presence of @Qualifier meta-annotation should be checked here?

            newQualifiers.add(q);
        }

        this.qualifiers = newQualifiers;
        return this;
    }

    public List<Annotation> getQualifiers() {
        return Collections.unmodifiableList(qualifiers);
    }

    /**
     * Set interceptors for a class
     * If called without any parameters - clears interceptors if any
     */
    public ClassCharacter<T> interceptedBy(Class<?>... interceptors) {
        return interceptedBy(Arrays.asList(interceptors));
    }

    public ClassCharacter<T> interceptedBy(Collection<Class<?>> interceptors) {
        if (interceptors == null || interceptors.isEmpty()) {
            throw new CharacterMetadataException("Provide at least one interceptor");
        }

        List<Class<?>> newInterceptors = new ArrayList<>();
        for (Class<?> interceptor : interceptors) {
            validateInterceptor(interceptor);
            newInterceptors.add(interceptor);
        }

        this.interceptors = newInterceptors;
        return this;
    }

    private void validateInterceptor(Class<?> interceptor) {
        if (interceptor == null) {
            throw new CharacterMetadataException("Interceptor can't be null");
        }

        if (!ReflectionUtils.isInstantiatable(interceptor)) {
            throw new CharacterMetadataException("Interceptor is not instantiatable");
        }

        if (interceptor == objectClass) {
            throw new CharacterMetadataException("Class can't be interceptor for itself");
        }
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
     * Interceptor bindings of a class
     * @return list of interceptor bindings, empty if there is no bindings
     * @see javax.interceptor.InterceptorBinding
     */
    public List<Annotation> getInterceptorBindings() {
        return Collections.unmodifiableList(interceptorBindings);
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

        throw new CharacterMetadataException(String.format("Interceptor method '%s' not found in '%s'." +
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
     * @throws CharacterMetadataException if method doesn't belong to a class
     * @see javax.interceptor.AroundConstruct
     * @see javax.interceptor.AroundInvoke
     * @see javax.annotation.PostConstruct
     * @see javax.annotation.PreDestroy
     */
    public ClassCharacter<T> intercepts(String type, Method method) {
        if (type == null) {
            throw new CharacterMetadataException("Type can't be null");
        }

        type = type.trim();
        if (type.isEmpty()) {
            throw new CharacterMetadataException("Type can't be empty");
        }

        if (method == null) {
            throw new CharacterMetadataException("Method can't be null");
        }

        if (method.getDeclaringClass() != objectClass) {
            throw new CharacterMetadataException(
                    String.format("Method '%s' doesn't belong to a class '%s'", method, objectClass));
        }

        // todo: validate method signature

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
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Provide a type");
        }

        if (interceptorMethods == null) {
            interceptorMethods = new HashMap<>();
        }

        type = type.trim();

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

    @Override
    public DefaultClassContainer<T> build(World world) {
        return new DefaultClassContainer<>(world, this);
    }
}
