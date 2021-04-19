package ahodanenok.di.character;

import ahodanenok.di.World;
import ahodanenok.di.container.impl.DefaultInterceptorContainer;
import ahodanenok.di.container.InterceptorContainer;
import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.metadata.ClassMetadataReader;
import ahodanenok.di.metadata.ExecutableMetadataReader;
import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.scope.SingletonScope;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// todo: remove duplication with ClassCharacter
public class InterceptorCharacter<T> implements Character<T> {

    public static <T> InterceptorCharacter<T> of(Class<T> clazz) {
        if (clazz == null) {
            throw new CharacterMetadataException("Class can't be null");
        }

        if (!ReflectionUtils.isInstantiatable(clazz)) {
            throw new CharacterMetadataException(String.format("Class '%s' is not instantiatable", clazz.getName()));
        }

        return new InterceptorCharacter<T>(clazz);
    }

    private final Class<T> objectClass;
    private final ClassMetadataReader<T> classMetadataReader;
    private final Scope<T> scope;
    private ExecutableMetadataReader constructor;

    private List<Annotation> interceptorBindings;
    private Map<String, Method> interceptorMethods;

    public InterceptorCharacter(Class<T> objectClass) {
        this.objectClass = objectClass;
        this.classMetadataReader = new ClassMetadataReader<>(objectClass);

        // todo: where to put mapping scopeName -> scope?
        String s = classMetadataReader.readScope();
        if (Singleton.class.getName().equals(s)) {
            this.scope = new SingletonScope<>();
        } else {
            this.scope = AlwaysNewScope.getInstance();
        }

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

    public Scope<T> getScope() {
        return scope;
    }

    public List<Annotation> getInterceptorBindings() {
        return interceptorBindings;
    }

    @SuppressWarnings("unchecked") // reader will have constructor from objectClass
    public Constructor<T> getConstructor() {
        if (constructor != null) {
            return (Constructor<T>) constructor.getExecutable();
        } else {
            return null;
        }
    }

    public InterceptorCharacter<T> intercepts(String type, Method method) {
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

    @Override
    public InterceptorContainer<T> build(World world) {
        // todo: make implementation selection customizable?
        return new DefaultInterceptorContainer<>(world, this);
    }
}
