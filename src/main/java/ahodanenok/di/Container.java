package ahodanenok.di;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class Container<T> {

    private World world;
    private Class<?> type;
    private Set<String> names;

    public Container(World world, Class<T> type, Set<String> names) {
        this.world = world;
        this.type = type;
        this.names = names;
    }

    public Class<?> getType() {
        return type;
    }

    public Set<String> getNames() {
        return names;
    }

    public Object getObject() {
        try {
            // todo: find appropriate constructor
            // todo: make class and constructor accessible
            return type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        throw new IllegalStateException();
    }
}
