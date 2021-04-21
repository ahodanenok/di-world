package ahodanenok.di.character;

import ahodanenok.di.World;
import ahodanenok.di.character.common.InjectableConstructor;
import ahodanenok.di.character.common.InterceptorMethods;
import ahodanenok.di.container.impl.DefaultInterceptorContainer;
import ahodanenok.di.container.InterceptorContainer;
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
import java.util.List;

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
    private final InjectableConstructor<T> constructor;

    private List<Annotation> interceptorBindings;
    private final InterceptorMethods<T> interceptorMethods;

    public InterceptorCharacter(Class<T> objectClass) {
        this.objectClass = objectClass;
        this.classMetadataReader = new ClassMetadataReader<>(objectClass);
        this.constructor = new InjectableConstructor<>(objectClass);

        // todo: remove duplication with ClassCharacter in determining scope
        // todo: where to put mapping scopeName -> scope?
        String s = classMetadataReader.readScope();
        if (Singleton.class.getName().equals(s)) {
            this.scope = new SingletonScope<>();
        } else {
            this.scope = AlwaysNewScope.getInstance();
        }

        this.interceptorBindings = classMetadataReader.readInterceptorBindings();
        this.interceptorMethods = new InterceptorMethods<>(objectClass);
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

    /**
     * @see InjectableConstructor
     */
    public void constructedBy(Constructor<?> constructor) {
        this.constructor.set(constructor);
    }

    /**
     * @see InjectableConstructor
     */
    public Constructor<T> getConstructor() {
        return constructor.get();
    }

    /**
     * @see InterceptorMethods#intercepts(InterceptorType, Method)
     */
    public InterceptorCharacter<T> intercepts(InterceptorType type, Method method) {
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
    public InterceptorContainer<T> build(World world) {
        // todo: make implementation selection customizable?
        return new DefaultInterceptorContainer<>(world, this);
    }
}
