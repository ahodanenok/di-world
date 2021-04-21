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
import ahodanenok.di.metadata.ExecutableMetadataReader;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundConstruct;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

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
        ExecutableMetadataReader constructorMetadataReader = new ExecutableMetadataReader(constructor);
        Object[] args = injector.resolveArguments(constructorMetadataReader);

        ConstructorInvocationContext constructorContext = new ConstructorInvocationContext(constructor);
        constructorContext.setParameters(args);

        try {

            // JSR-318 (Interceptors 1.2), 3.3
            // The set of interceptor bindings for a method or constructor
            // are those applied to the target class combined with those
            // applied at method level or constructor level.

            // JSR-318 (Interceptors 1.2), 3.3
            // An interceptor binding declared on a method or constructor
            // replaces an interceptor binding of the same type declared
            // at class level or inherited from a superclass

            InterceptorChain aroundConstructChain = world.getInterceptorChain(
                    InterceptorRequest.of(AroundConstruct.class.getName())
                            .withClasses(character.getInterceptors())
                            .withBindings(ReflectionUtils.combineAnnotations(
                                    constructorMetadataReader.readInterceptorBindings(),
                                    character.getInterceptorBindings()))
            );
            Object instance = aroundConstructChain.invoke(constructorContext);

            injector.inject(instance);

            InvocationContext postConstructContext;
            Method interceptorMethod = character.getInterceptorMethod(PostConstruct.class.getName());
            if (interceptorMethod != null) {
                postConstructContext = new MethodInvocationContext(instance, interceptorMethod);
            } else {
                postConstructContext = new ObjectInvocationContext(instance);
            }

            InterceptorChain postConstructChain = world.getInterceptorChain(
                    InterceptorRequest.of(PostConstruct.class.getName())
                            .withClasses(character.getInterceptors())
                            .withBindings(ReflectionUtils.combineAnnotations(
                                    interceptorMethod != null
                                            ? new ExecutableMetadataReader(interceptorMethod).readInterceptorBindings()
                                            : Collections.emptyList(),
                                    character.getInterceptorBindings()))
            );

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
