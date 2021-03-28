package ahodanenok.di;

import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.interceptor.context.ConstructorInvocationContext;
import ahodanenok.di.scope.Scope;

import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Container<T> {

    private World world;
    private Class<T> objectClass;
    private Set<String> names;
    private Scope<T> scope;
    private boolean interceptor;
    private List<Class<?>> interceptedBy;

    // todo: unmodifiable names
    public Container(World world, Class<T> objectClass, Set<String> names, Scope<T> scope, boolean interceptor, List<Class<?>> interceptedBy) {
        this.world = world;
        this.objectClass = objectClass;
        this.names = names;
        this.scope = scope;
        this.interceptor = interceptor;
        this.interceptedBy = interceptedBy;
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
        // todo: scope
        Constructor<?> constructor = getConstructor();

        Object[] args = new Object[constructor.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            args[i] = resolveArgument(constructor, i);
        }

        ConstructorInvocationContext context = new ConstructorInvocationContext(constructor);
        context.setParameters(args);

        // todo: intercept around construct
        // todo: intercept post construct

        try {
            // todo: suppress warning
            if (!interceptor) {
                InterceptorChain aroundConstructChain = world.getInterceptorChain(
                        InterceptorRequest.ofType(AroundConstruct.class.getName()).withClasses(interceptedBy));

                return (T) aroundConstructChain.invoke(context);
            } else {
                return (T) context.proceed();
            }
        } catch (Exception e) {
            // todo: handle exceptions
            throw new IllegalStateException(e);
        }
    }

    private Constructor<?> getConstructor() {
        // todo: additional rules for selecting constructors from class

        List<Constructor<?>> matched = new ArrayList<>();
        for (Constructor<?> c : objectClass.getDeclaredConstructors()) {
            // todo: only @Inject is used to mark injectable constructors?
            if (c.getDeclaredAnnotation(Inject.class) != null) {
                matched.add(c);
            }
        }

        if (matched.isEmpty()) {
            try {
                // todo: any public? no-arg is fallback?
                matched.add(objectClass.getDeclaredConstructor());
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

    private Object resolveArgument(Executable e, int pos) {
        // todo: intercept around resolve
        Class<?> paramType = e.getParameterTypes()[pos];
        return world.find(ObjectRequest.byType(paramType));
    }
}
