package ahodanenok.di.container;

import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.interceptor.context.ConstructorInvocationContext;
import ahodanenok.di.interceptor.context.MethodInvocationContext;
import ahodanenok.di.interceptor.context.ObjectInvocationContext;
import ahodanenok.di.metadata.ExecutableMetadataReader;
import ahodanenok.di.metadata.FieldMetadataReader;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.interceptor.AroundConstruct;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class ClassContainer<T> {

    private World world;
    private ClassCharacter<T> character;

    private Class<T> objectClass;
    private Set<String> names;
    private Scope<T> scope;
    private Constructor<?> constructor;

    public ClassContainer(World world, ClassCharacter<T> character) {
        this.world = world;
        this.character = character;

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

    public Set<Annotation> getQualifiers() {
        return character.getQualifiers();
    }

    public T getObject() {
        return scope.getObject(this::doGetObject);
    }

    private T doGetObject() {
        if (constructor == null) {
            constructor = resolveConstructor();
        }

        // todo: cache arguments?
        Object[] args = resolveArguments(constructor);

        ConstructorInvocationContext constructorContext = new ConstructorInvocationContext(constructor);
        constructorContext.setParameters(args);

        // todo: split to InterceptorContainer and ClassContainer???

        try {
            Object instance;
            if (!character.isInterceptor()) {
                InterceptorChain aroundConstructChain = world.getInterceptorChain(
                        InterceptorRequest.of(AroundConstruct.class.getName()).withClasses(character.getInterceptors()));
                instance = aroundConstructChain.invoke(constructorContext);
            } else {
                instance = constructorContext.proceed();
            }

            inject(instance);

            if (!character.isInterceptor()) {
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
            }

            // todo: interceptors could swap created instance for something else, return Object?
            // @SuppressWarnings("unchecked")
            T castedInstance = (T) instance;

            return castedInstance;
        } catch (Exception e) {
            // todo: handle exceptions
            throw new IllegalStateException(e);
        }
    }

    private Constructor<?> resolveConstructor() {
        if (character.getConstructor() != null) {
            return character.getConstructor();
        }

        Constructor<?>[] constructors = objectClass.getConstructors();
        // If there a single public constructor, using it
        if (constructors.length == 1) {
            return constructors[0];
        }

        try {
            // falling back to no-arg public constructor
            return objectClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new CharacterMetadataException(String.format(
                    "Couldn't resolve constructor for '%s', provide it explicitly in a character" +
                    " or use @Inject annotation to mark which constructor to use", objectClass));
        }
    }

    private Object[] resolveArguments(Executable executable) {
        ExecutableMetadataReader metadataReader = new ExecutableMetadataReader(executable);

        Object[] args = new Object[executable.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            args[i] = resolveArgument(metadataReader, i);
        }

        return args;
    }

    private Object resolveArgument(ExecutableMetadataReader metadataReader, int paramNum) {
        // todo: support injecting list of dependencies
        Class<?> paramType = metadataReader.getExecutable().getParameterTypes()[paramNum];
        if (Provider.class.equals(paramType)) {
            paramType = (Class<?>) ((ParameterizedType) metadataReader
                        .getExecutable().getGenericParameterTypes()[paramNum])
                    .getActualTypeArguments()[0];
        }

        ObjectRequest<?> request = ObjectRequest.of(paramType);
        request.withContext(metadataReader.getExecutable() + " "  + metadataReader.getExecutable().getParameters()[paramNum]);

        String name = metadataReader.readParameterName(paramNum);
        if (name != null) {
            request.withName(name);
        }

        List<Annotation> qualifiers = metadataReader.readParameterQualifiers(paramNum);
        if (!qualifiers.isEmpty()) {
            request.withQualifiers(qualifiers);
        }

        // todo: intercept around resolve
        if (Provider.class.equals(metadataReader.getExecutable().getParameterTypes()[paramNum])) {
            return (Provider<?>) () -> world.find(request);
        } else {
            return world.find(request);
        }
    }

    private Object resolvedDependency(Field field) {
        // todo: support injecting list of dependencies

        FieldMetadataReader metadataReader = new FieldMetadataReader(field);

        Class<?> paramType = field.getType();
        if (Provider.class.equals(paramType)) {
            paramType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        }

        ObjectRequest<?> request = ObjectRequest.of(paramType);
        request.withContext(field);

        String name = metadataReader.readName();
        if (name != null) {
            request.withName(name);
        }

        List<Annotation> qualifiers = metadataReader.readQualifiers();
        if (!qualifiers.isEmpty()) {
            request.withQualifiers(qualifiers);
        }

        // todo: intercept around resolve
        if (Provider.class.equals(field.getType())) {
            return (Provider<?>) () -> world.find(request);
        } else {
            return world.find(request);
        }
    }

    // todo: how to handle exception?
    private void inject(Object instance) throws Exception {
        Map<Class<?>, List<Method>> methodsByClass = ReflectionUtils.getInstanceMethods(instance.getClass())
                .stream().collect(Collectors.groupingBy(Method::getDeclaringClass));

        // todo: read about injection rules, for now as i remember it
        for (Class<?> clazz : ReflectionUtils.getInheritanceChain(instance.getClass())) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Inject.class)) {
                    // todo: make accessible only when needed
                    f.setAccessible(true);
                    f.set(instance, resolvedDependency(f));
                }
            }

            for (Method m : methodsByClass.getOrDefault(clazz, Collections.emptyList())) {
                if (m.isAnnotationPresent(Inject.class)) {
                    // todo: make accessible only when needed
                    m.setAccessible(true);
                    m.invoke(instance, resolveArguments(m));
                }
            }
        }
    }
}
