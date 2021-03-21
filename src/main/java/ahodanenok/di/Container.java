package ahodanenok.di;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Container<T> {

    private World world;
    private Class<T> type;
    private Set<String> names;

    // todo: unmodifiable names
    public Container(World world, Class<T> type, Set<String> names) {
        this.world = world;
        this.type = type;
        this.names = names;
    }

    public Class<T> getType() {
        return type;
    }

    public Set<String> getNames() {
        return names;
    }

    public Object getObject() {
        // todo: scope
        Constructor<?> constructor = getConstructor();

        Object[] args = new Object[constructor.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            args[i] = resolveArgument(constructor, i);
        }

        // todo: intercept around construct
        // todo: intercept post construct

        try {
            // todo: make class and constructor accessible
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // todo: handle exceptions
            throw new IllegalStateException(e);
        }
    }

    private Constructor<?> getConstructor() {
        // todo: additional rules for selecting constructors from class

        List<Constructor<?>> matched = new ArrayList<>();
        for (Constructor<?> c : type.getDeclaredConstructors()) {
            // todo: only @Inject is used to mark injectable constructors?
            if (c.getDeclaredAnnotation(Inject.class) != null) {
                matched.add(c);
            }
        }

        if (matched.isEmpty()) {
            try {
                // todo: any public? no-arg is fallback?
                matched.add(type.getDeclaredConstructor());
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
