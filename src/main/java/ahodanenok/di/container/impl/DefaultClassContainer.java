package ahodanenok.di.container.impl;

import ahodanenok.di.*;
import ahodanenok.di.augment.Augmentation;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.container.EventHandlerContainer;
import ahodanenok.di.container.InjectableContainer;
import ahodanenok.di.event.EventHandler;
import ahodanenok.di.exception.ObjectRetrievalException;
import ahodanenok.di.inject.Injector;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.interceptor.InterceptorType;
import ahodanenok.di.interceptor.context.ConstructorInvocationContext;
import ahodanenok.di.interceptor.context.MethodInvocationContext;
import ahodanenok.di.interceptor.context.ObjectInvocationContext;
import ahodanenok.di.metadata.ExecutableMetadataReader;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.util.ReflectionUtils;

import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class DefaultClassContainer<T> implements InjectableContainer<T>, EventHandlerContainer<T> {

    private WorldInternals world;
    private ClassCharacter<T> character;
    private Injector injector;

    private Class<T> objectClass;
    private Set<String> names;
    private Scope<T> scope;

    public DefaultClassContainer(WorldInternals world, ClassCharacter<T> character) {
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
        Augmentation augmentation = world.requestAugmentation();

        Constructor<?> constructor = augmentation.augmentBeforeInstantiated(character, character.getConstructor());

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
                    InterceptorRequest.of(InterceptorType.AROUND_CONSTRUCT)
                            .withClasses(character.getInterceptors())
                            .withBindings(ReflectionUtils.combineAnnotations(
                                    constructorMetadataReader.readInterceptorBindings(),
                                    character.getInterceptorBindings()))
            );
            Object instance = augmentation.augmentAfterInstantiated(
                    character, aroundConstructChain.invoke(constructorContext));

            injector.inject(instance);
            instance = augmentation.augmentAfterInjected(character, instance);

            InvocationContext postConstructContext;
            Method interceptorMethod = character.getInterceptorMethod(InterceptorType.POST_CONSTRUCT);
            if (interceptorMethod != null) {
                postConstructContext = new MethodInvocationContext(instance, interceptorMethod);
            } else {
                postConstructContext = new ObjectInvocationContext(instance);
            }

            InterceptorChain postConstructChain = world.getInterceptorChain(
                    InterceptorRequest.of(InterceptorType.POST_CONSTRUCT)
                            .withClasses(character.getInterceptors())
                            .withBindings(ReflectionUtils.combineAnnotations(
                                    interceptorMethod != null
                                            ? new ExecutableMetadataReader(interceptorMethod).readInterceptorBindings()
                                            : Collections.emptyList(),
                                    character.getInterceptorBindings()))
            );

            postConstructChain.invoke(postConstructContext);

            // todo: interceptors/augmentation could swap created instance for something else, return Object?
            // @SuppressWarnings("unchecked")
            T castedInstance = (T) augmentation.augmentAfterConstructed(character, instance);

            return castedInstance;
        } catch (Exception e) {
             throw new ObjectRetrievalException(
                    String.format("Can't get object of type '%s'", objectClass.getName()), e);
        }
    }

    @Override
    public List<EventHandler> getEventHandlers() {
        // todo: retrieve event handlers from class
        return Collections.emptyList();
    }
}
