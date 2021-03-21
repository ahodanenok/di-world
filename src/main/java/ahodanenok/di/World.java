package ahodanenok.di;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class World implements Iterable<Container> {

    public static void main(String[] args) {

    }

    private List<Container> containers = new ArrayList<>();
    private EntranceQueue queue = new EntranceQueue(this::register);

    public EntranceQueue getQueue() {
        return queue;
    }

    private void register(List<ContainerConfiguration> configs) {
        for (ContainerConfiguration config : configs) {
            register(buildContainer(config));
        }
    }

    private void register(Container container) {
        containers.add(container);
    }

    private Container buildContainer(ContainerConfiguration config) {
        // todo: configuration class per container type (class, factory method, instance)
        // todo: configuration instantiates container of the appropriate type and later world is bound - c.bind(world)
        Container container = new Container(this, config.getType(), config.getNames());

        return container;
    }

    @Override
    public Iterator<Container> iterator() {
        return Collections.unmodifiableCollection(containers).iterator();
    }
}
