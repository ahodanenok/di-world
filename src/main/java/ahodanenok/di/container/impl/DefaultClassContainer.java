package ahodanenok.di.container.impl;

import ahodanenok.di.*;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.container.Container;
import ahodanenok.di.container.InjectableContainer;
import ahodanenok.di.exception.ObjectRetrievalException;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.interceptor.context.ConstructorInvocationContext;
import ahodanenok.di.interceptor.context.MethodInvocationContext;
import ahodanenok.di.interceptor.context.ObjectInvocationContext;
import ahodanenok.di.scope.Scope;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundConstruct;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

// todo: think of possible name alternatives
public class DefaultClassContainer<T> implements Container<T>, InjectableContainer<T> {

    private World world;
    private ClassCharacter<T> character;
    private Injector injector;

    private Class<T> objectClass;
    private Set<String> names;
    private Scope<T> scope;

    public DefaultClassContainer(World world, ClassCharacter<T> character) {
        this.world = world;
        this.character = character;
        this.injector = new Injector(world);

        this.objectClass = character.getObjectClass();
        this.names = character.getNames();
        this.scope = character.getScope();
    }

    public Class<T> getObjectClass() {
        return objectClass;
    }

    public Set<String> getNames() {
        return names;
    }

    public List<Annotation> getQualifiers() {
        return character.getQualifiers();
    }

    public T getObject() {
        return scope.getObject(this::doGetObject);
    }

    private T doGetObject() {
        Constructor<T> constructor = character.getConstructor();

        // todo: cache arguments?
        Object[] args = injector.resolveArguments(constructor);

        ConstructorInvocationContext constructorContext = new ConstructorInvocationContext(constructor);
        constructorContext.setParameters(args);

        try {
            InterceptorChain aroundConstructChain = world.getInterceptorChain(
                    InterceptorRequest.of(AroundConstruct.class.getName()).withClasses(character.getInterceptors()));
            Object instance = aroundConstructChain.invoke(constructorContext);

            injector.inject(instance);

            InterceptorChain postConstructChain = world.getInterceptorChain(
                    InterceptorRequest.of(PostConstruct.class.getName()).withClasses(character.getInterceptors()));

            InvocationContext postConstructContext;
            Method interceptorMethod = character.getInterceptorMethod(PostConstruct.class.getName());
            if (interceptorMethod != null) {
                postConstructContext = new MethodInvocationContext(instance, interceptorMethod);
            } else {
                postConstructContext = new ObjectInvocationContext(instance);
            }

            postConstructChain.invoke(postConstructContext);

            // todo: interceptors could swap created instance for something else, return Object?
            // @SuppressWarnings("unchecked")
            T castedInstance = (T) instance;

            return castedInstance;
        } catch (Exception e) {
             throw new ObjectRetrievalException(
                    String.format("Can't get object of type '%s'", objectClass.getName()), e);
        }
    }
}
