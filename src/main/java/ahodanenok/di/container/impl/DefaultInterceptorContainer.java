package ahodanenok.di.container.impl;

import ahodanenok.di.World;
import ahodanenok.di.character.InterceptorCharacter;
import ahodanenok.di.container.InterceptorContainer;
import ahodanenok.di.interceptor.Interceptor;
import ahodanenok.di.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class DefaultInterceptorContainer<T> implements InterceptorContainer<T> {

    private final World world;
    private final InterceptorCharacter<T> character;

    public DefaultInterceptorContainer(World world, InterceptorCharacter<T> character) {
        this.world = world;
        this.character = character;
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
    public Interceptor getInterceptor(String type) {
        Method method = character.getInterceptorMethod(type);
        if (method == null) {
            return null;
        }

        return new World.InterceptorInvoke(this, method);
    }

    @Override
    public T getObject() {
        Constructor<T> constructor = character.getConstructor();

        // todo: resolve constructor arguments
        // todo: create instance
        // todo: inject instance

        try {
            return ReflectionUtils.newInstance(constructor);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
