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

            InterceptorChain aroundConstructChain = getInterceptorChain(InterceptorType.AROUND_CONSTRUCT, constructor);
            Object instance = augmentation.augmentAfterInstantiated(
                    character, aroundConstructChain.invoke(constructorContext));

            injector.inject(instance);
            instance = augmentation.augmentAfterInjected(character, instance);

            InvocationContext postConstructContext;
            Method postConstructMethod = character.getInterceptorMethod(InterceptorType.POST_CONSTRUCT);
            if (postConstructMethod != null) {
                postConstructContext = new MethodInvocationContext(instance, postConstructMethod);
            } else {
                postConstructContext = new ObjectInvocationContext(instance);
            }

            InterceptorChain postConstructChain
                    = getInterceptorChain(InterceptorType.POST_CONSTRUCT, postConstructMethod);
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

    @Override
    public void destroy() {
        Method preDestroyMethod = character.getInterceptorMethod(InterceptorType.PRE_DESTROY);
        InterceptorChain preDestroyChain = getInterceptorChain(InterceptorType.PRE_DESTROY, preDestroyMethod);
        if (!preDestroyChain.getInterceptors().isEmpty()) {
            InvocationContext preDestroyContext;
            Method interceptorMethod = character.getInterceptorMethod(InterceptorType.POST_CONSTRUCT);
            if (interceptorMethod != null) {
                preDestroyContext = new MethodInvocationContext(getObject(), interceptorMethod);
            } else {
                preDestroyContext = new ObjectInvocationContext(getObject());
            }

            try {
                preDestroyChain.invoke(preDestroyContext);
            } catch (Exception e) {
                // todo: exception+message
                throw new RuntimeException(e);
            }
        }

        scope.destroy();
    }

    private InterceptorChain getInterceptorChain(InterceptorType type, Executable method) {
        return world.getInterceptorChain(
                InterceptorRequest.of(type)
                        .withClasses(character.getInterceptors())
                        .withBindings(ReflectionUtils.combineAnnotations(
                                method != null
                                        ? new ExecutableMetadataReader(method).readInterceptorBindings()
                                        : Collections.emptyList(),
                                character.getInterceptorBindings())));
    }
}
