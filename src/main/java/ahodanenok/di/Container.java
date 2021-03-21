package ahodanenok.di;

import java.util.Set;

public class Container {

    private World world;
    private Class<?> type;
    private Set<String> names;

    public Container(World world, Class<?> type, Set<String> names) {
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
}
