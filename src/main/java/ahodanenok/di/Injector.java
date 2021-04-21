package ahodanenok.di;

import ahodanenok.di.exception.DependencyInjectionException;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.interceptor.context.InjectionPointInvocationContext;
import ahodanenok.di.metadata.ExecutableMetadataReader;
import ahodanenok.di.metadata.FieldMetadataReader;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Provider;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public final class Injector {

    private final World world;

    public Injector(World world) {
        this.world = world;
    }

    public void inject(Object instance) throws Exception {
        Map<Class<?>, List<Method>> methodsByClass = ReflectionUtils.getInstanceMethods(instance.getClass())
                .stream().collect(Collectors.groupingBy(Method::getDeclaringClass));

        for (Class<?> clazz : ReflectionUtils.getInheritanceChain(instance.getClass())) {
            for (Field f : clazz.getDeclaredFields()) {
                FieldMetadataReader metadataReader = new FieldMetadataReader(f);
                if (metadataReader.readInjectable()) {
                    InjectionPoint injectionPoint = new InjectionPoint(f, metadataReader.readQualifiers());
                    ReflectionUtils.setField(f, instance, resolveDependency(injectionPoint));
                }
            }

            for (Method m : methodsByClass.getOrDefault(clazz, Collections.emptyList())) {
                ExecutableMetadataReader metadataReader = new ExecutableMetadataReader(m);
                if (metadataReader.readInjectable()) {
                    ReflectionUtils.invoke(m, instance, resolveArguments(metadataReader));
                }
            }
        }
    }

    public Object[] resolveArguments(Executable executable) {
        return resolveArguments(new ExecutableMetadataReader(executable));
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
                // technically it is possible to inject Provider<InjectionPoint>, but its lazy nature
                // makes it impossible to retrieve underlying injection point as it will be long gone
                // when Provider#get() will be finally invoked
                return (Provider<?>) () -> resolveDependency(injectionPoint, objectType, optional, false);
            } else if (rawType == Optional.class) {
                if (multiple) {
                    throw new DependencyInjectionException(String.format(
                            "Injecting collection of optionals is not supported, target is '%s'",
                            injectionPoint.getTarget()));
                }

                Type objectType = parameterizedType.getActualTypeArguments()[0];
                return Optional.ofNullable(resolveDependency(injectionPoint, objectType, true, false));
            } else if (rawType instanceof Class<?> && Collection.class.isAssignableFrom((Class<?>) rawType)) {
                Type objectType = parameterizedType.getActualTypeArguments()[0];
                Collection<?> collection =
                        (Collection<?>) resolveDependency(injectionPoint, objectType, optional, true);

                Class<?> collectionType = (Class<?>) rawType;
                if (Collection.class == collectionType) {
                    return collection;
                } else if (List.class == collectionType) {
                    return new ArrayList<>(collection);
                } else if (Set.class == collectionType) {
                    return new LinkedHashSet<>(collection);
                } else {
                    throw new DependencyInjectionException(String.format(
                            "Injecting collection type '%s' is not supported, target is '%s'",
                            collectionType.getName(), injectionPoint.getTarget()));
                }
            } else {
                // todo: support generic object types?

                throw new DependencyInjectionException(String.format(
                        "Injection of type '%s' is not supported, target is '%s' ",
                        rawType.getTypeName(), injectionPoint.getTarget()));
            }
        } else if (type instanceof Class<?>){
            Class<?> objectType = (Class<?>) type;
            ObjectRequest<?> request = ObjectRequest.of(objectType).withQualifiers(injectionPoint.getQualifiers());

            if (optional) {
                request.optional();
            }

            if (type != InjectionPoint.class) {
                world.pushInjectionPoint(injectionPoint);
            }

            try {
                InterceptorChain aroundInjectChain = world.getInterceptorChain(
                        InterceptorRequest.of(AroundInject.class.getName()).matchAll());

                // looks like a hack for me to use invocation context in a such way
                // but it's nice to use the existing interceptors mechanism
                return aroundInjectChain.invoke(new InjectionPointInvocationContext(injectionPoint, () -> {
                    if (multiple) {
                        return world.findAll(request);
                    } else {
                        return world.find(request);
                    }
                }));
            } catch (Exception e) {
                throw new DependencyInjectionException(
                        "Dependency lookup failed during processing interceptor chain", e);
            } finally {
                if (type != InjectionPoint.class) {
                    world.popInjectionPoint();
                }
            }
        } else {
            throw new DependencyInjectionException(String.format(
                    "Injection of type '%s' is not supported, target is '%s' ",
                    type.getTypeName(), injectionPoint.getTarget()));
        }
    }
}
