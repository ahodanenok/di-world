package ahodanenok.di.container;

import ahodanenok.di.InjectionPoint;
import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.exception.DependencyInjectionException;
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
import javax.inject.Provider;
import javax.interceptor.AroundConstruct;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

// todo: think of possible name alternatives
// todo: split to ClassContainer and InterceptorContainer
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

    public List<Annotation> getQualifiers() {
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
        Object[] args = resolveArguments(new ExecutableMetadataReader(constructor));

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
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

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

    private Object[] resolveArguments(ExecutableMetadataReader metadataReader) {

        Object[] args = new Object[metadataReader.getExecutable().getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            InjectionPoint injectionPoint = new InjectionPoint(
                    metadataReader.getExecutable(), i, metadataReader.readParameterQualifiers(i));

            args[i] = resolveDependency(injectionPoint);
        }

        return args;
    }

    private Object resolveDependency(InjectionPoint injectionPoint) {
        return resolveDependency(injectionPoint, injectionPoint.getGenericType(), false, false);
    }

    private Object resolveDependency(InjectionPoint injectionPoint, Type type, boolean optional, boolean multiple) {
//        world.pushInjectionPoint(injectionPoint);
        try {
            // todo: around dependency resolution
//            InterceptorChain aroundInjectChain = world.getInterceptorChain(
//                    InterceptorRequest.of("AroundInject").matchAll());
//
//            Object value = aroundInjectChain.invoke(new DependencyLookupInvocationContext(request));

            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                if (rawType == Provider.class) {
                    if (multiple) {
                        throw new DependencyInjectionException(String.format(
                                "Injecting collection of providers is not supported, target = '%s'",
                                injectionPoint.getTarget()));
                    }

                    Type objectType = parameterizedType.getActualTypeArguments()[0];
                    return (Provider<?>) () -> resolveDependency(injectionPoint, objectType, optional, false);
                } else if (rawType == Optional.class) {
                    if (multiple) {
                        throw new DependencyInjectionException(String.format(
                                "Injecting collection of optionals is not supported, target = '%s'",
                                injectionPoint.getTarget()));
                    }

                    Type objectType = parameterizedType.getActualTypeArguments()[0];
                    return Optional.ofNullable(resolveDependency(injectionPoint, objectType, true, false));
                } else if (rawType instanceof Class<?> && Collection.class.isAssignableFrom((Class<?>) rawType)) {
                    Type objectType = parameterizedType.getActualTypeArguments()[0];
                    return resolveDependency(injectionPoint, objectType, optional, true);
                } else {
                    // todo: support generic object types?

                    // todo: exception + message
                    throw new IllegalStateException();
                }
            } else if (type instanceof Class<?>){
                Class<?> objectType = (Class<?>) type;
                ObjectRequest<?> request = ObjectRequest.of(objectType).withQualifiers(injectionPoint.getQualifiers());

                if (optional) {
                    request.optional();
                }

                if (multiple) {
                    return world.findAll(request);
                } else {
                    return world.find(request);
                }
            } else {
                // todo: exception + message
                throw new IllegalStateException(type.toString());
            }
        } finally {
//            world.popInjectionPoint(injectionPoint);
        }
    }

    // todo: how to handle exception?
    private void inject(Object instance) throws Exception {
        Map<Class<?>, List<Method>> methodsByClass = ReflectionUtils.getInstanceMethods(instance.getClass())
                .stream().collect(Collectors.groupingBy(Method::getDeclaringClass));

        for (Class<?> clazz : ReflectionUtils.getInheritanceChain(instance.getClass())) {
            for (Field f : clazz.getDeclaredFields()) {
                FieldMetadataReader metadataReader = new FieldMetadataReader(f);

                if (metadataReader.readInjectable()) {
                    InjectionPoint injectionPoint = new InjectionPoint(f, metadataReader.readQualifiers());

                    // todo: make accessible only when needed
                    f.setAccessible(true);
                    f.set(instance, resolveDependency(injectionPoint));
                }
            }

            for (Method m : methodsByClass.getOrDefault(clazz, Collections.emptyList())) {
                ExecutableMetadataReader metadataReader = new ExecutableMetadataReader(m);
                if (metadataReader.readInjectable()) {
                    // todo: make accessible only when needed
                    m.setAccessible(true);
                    m.invoke(instance, resolveArguments(metadataReader));
                }
            }
        }
    }
}
