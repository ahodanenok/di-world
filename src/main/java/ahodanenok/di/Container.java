package ahodanenok.di;

import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.interceptor.context.ConstructorInvocationContext;
import ahodanenok.di.scope.Scope;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class Container<T> {

    private World world;
    private ClassCharacter<T> character;

    private Class<T> objectClass;
    private Set<String> names;
    private Scope<T> scope;

    public Container(World world, ClassCharacter<T> character) {
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

    public T getObject() {
        return scope.getObject(this::doGetObject);
    }

    private T doGetObject() {
        Constructor<?> constructor = getConstructor();
        Object[] args = resolveArguments(constructor);

        ConstructorInvocationContext context = new ConstructorInvocationContext(constructor);
        context.setParameters(args);

        // todo: intercept post construct

        try {
            // todo: suppress warning
            T instance;
            if (!character.isInterceptor()) {
                InterceptorChain aroundConstructChain = world.getInterceptorChain(
                        InterceptorRequest.ofType(AroundConstruct.class.getName()).withClasses(character.getInterceptors()));

                instance = (T) aroundConstructChain.invoke(context);
            } else {
                instance = (T) context.proceed();
            }

            inject(instance);

            return instance;
        } catch (Exception e) {
            // todo: handle exceptions
            throw new IllegalStateException(e);
        }
    }

    private Constructor<?> getConstructor() {
        // todo: additional rules for selecting constructors from class

        List<Constructor<?>> matched = new ArrayList<>();
        for (Constructor<?> c : getObjectClass().getDeclaredConstructors()) {
            // todo: only @Inject is used to mark injectable constructors?
            if (c.getDeclaredAnnotation(Inject.class) != null) {
                matched.add(c);
            }
        }

        if (matched.isEmpty()) {
            try {
                // todo: any public? no-arg is fallback?
                matched.add(getObjectClass().getDeclaredConstructor());
            } catch (NoSuchMethodException e) {
                // todo: exception
                e.printStackTrace();
                throw new IllegalStateException("no constructor");
            }
        }

        if (matched.size() == 1) {
            return matched.get(0);
        } else {
            // todo: exception
            throw new IllegalStateException("multiple constructors");
        }
    }

    private Object[] resolveArguments(Executable executable) {
        Object[] args = new Object[executable.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            args[i] = resolveArgument(executable, i);
        }

        return args;
    }

    private Object resolveArgument(Executable executable, int pos) {
        Class<?> paramType = executable.getParameterTypes()[pos];
        // todo: intercept around resolve
        // todo: by name
        // todo: qualifiers
        return world.find(ObjectRequest.byType(paramType));
    }

    private Object resolvedDependency(Field field) {
        // todo: intercept around resolve
        // todo: by name
        // todo: qualifiers
        return world.find(ObjectRequest.byType(field.getType()));
    }

    // todo: how to handle exception?
    private void inject(T instance) throws Exception {
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
