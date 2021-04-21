package ahodanenok.di.container.impl;

import ahodanenok.di.inject.Injector;
import ahodanenok.di.World;
import ahodanenok.di.character.InterceptorCharacter;
import ahodanenok.di.container.InterceptorContainer;
import ahodanenok.di.exception.ObjectRetrievalException;
import ahodanenok.di.interceptor.Interceptor;
import ahodanenok.di.interceptor.InterceptorInvoke;
import ahodanenok.di.interceptor.InterceptorType;
import ahodanenok.di.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class DefaultInterceptorContainer<T> implements InterceptorContainer<T> {

    // todo: reference to a world is not needed?
    private final World world;
    private final InterceptorCharacter<T> character;
    private final Injector injector;

    public DefaultInterceptorContainer(World world, InterceptorCharacter<T> character) {
        this.world = world;
        this.character = character;
        this.injector = new Injector(world);
    }

    @Override
    public Class<T> getObjectClass() {
        return character.getObjectClass();
    }

    @Override
    public List<Annotation> getInterceptorBindings() {
        return character.getInterceptorBindings();
    }

    @Override
    public Interceptor getInterceptor(InterceptorType type) {
        Method method = character.getInterceptorMethod(type);
        if (method == null) {
            return null;
        }

        return new InterceptorInvoke(this, method);
    }

    @Override
    public T getObject() {
        Constructor<T> constructor = character.getConstructor();
        Object[] args = injector.resolveArguments(constructor);

        try {
            T instance = ReflectionUtils.newInstance(constructor, args);

            injector.inject(instance);

            return instance;
        } catch (Exception e) {
            throw new ObjectRetrievalException(
                    String.format("Can't get interceptor of type '%s'", getObjectClass().getName()), e);
        }
    }
}
