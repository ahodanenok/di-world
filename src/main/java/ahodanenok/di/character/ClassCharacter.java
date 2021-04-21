package ahodanenok.di.character;

import ahodanenok.di.World;
import ahodanenok.di.character.common.InjectableConstructor;
import ahodanenok.di.character.common.InterceptorMethods;
import ahodanenok.di.container.impl.DefaultClassContainer;
import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.interceptor.InterceptorType;
import ahodanenok.di.metadata.ClassMetadataReader;
import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.scope.SingletonScope;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

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
    private final InjectableConstructor<T> constructor;
    private List<Annotation> qualifiers;

    private List<Class<?>> interceptors;
    private List<Annotation> interceptorBindings;
    private InterceptorMethods<T> interceptorMethods;

    public ClassCharacter(Class<T> clazz) {
        this.classMetadataReader = new ClassMetadataReader<>(clazz);
        this.objectClass = clazz;
        this.constructor = new InjectableConstructor<>(clazz);

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

        this.interceptors = classMetadataReader.readInterceptors();
        this.interceptors.forEach(this::validateInterceptor);
        this.interceptorBindings = classMetadataReader.readInterceptorBindings();
        this.interceptorMethods = new InterceptorMethods<>(clazz);
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
     * @see InjectableConstructor
     */
    public ClassCharacter<T> constructedBy(Constructor<?> constructor) {
        this.constructor.set(constructor);
        return this;
    }

    /**
     * @see InjectableConstructor
     */
    public Constructor<T> getConstructor() {
        return constructor.get();
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
     * @see InterceptorMethods#intercepts(InterceptorType, String)
     */
    public ClassCharacter<T> intercepts(InterceptorType type, String methodName) {
        interceptorMethods.intercepts(type, methodName);
        return this;
    }

    /**
     * @see InterceptorMethods#intercepts(InterceptorType, Method)
     */
    public ClassCharacter<T> intercepts(InterceptorType type, Method method) {
        interceptorMethods.intercepts(type, method);
        return this;
    }

    /**
     * @see InterceptorMethods#get(InterceptorType)
     */
    public Method getInterceptorMethod(InterceptorType type) {
        return interceptorMethods.get(type);
    }

    @Override
    public DefaultClassContainer<T> build(World world) {
        return new DefaultClassContainer<>(world, this);
    }
}
