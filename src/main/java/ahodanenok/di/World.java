package ahodanenok.di;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class World implements Iterable<Container<?>> {

    public static void main(String[] args) {

    }

    private List<Container<?>> containers = new ArrayList<>();
    private EntranceQueue queue = new EntranceQueue(this::register);

    public EntranceQueue getQueue() {
        return queue;
    }

    private void register(List<ContainerConfiguration> configs) {
        for (ContainerConfiguration config : configs) {
            register(buildContainer(config));
        }
    }

    private void register(Container<?> container) {
        for (Container<?> c : containers) {
            for (String n : c.getNames()) {
                if (container.getNames().contains(n)) {
                    throw new IllegalStateException(n);
                }
            }
        }

        containers.add(container);
    }

    private Container buildContainer(ContainerConfiguration config) {
        // todo: configuration class per container type (class, factory method, instance)
        // todo: configuration instantiates container of the appropriate type and later world is bound - c.bind(world)
        Container<?> container = new Container<>(this, config.getType(), config.getNames());

        return container;
    }

    public <T> T find(ObjectRequest<T> request) {
        List<Container<?>> containers = findContainers(request);
        if (containers.size() == 1) {
            // todo: supress unchecked
            return (T) containers.get(0).getObject();
        } else {
            // todo: exception+message
            throw new IllegalStateException("multiple");
        }
    }

    public <T> List<T> findAll(ObjectRequest<T> request) {
        // todo: supress unchecked
        return findContainers(request).stream().map(c -> (T) c.getObject()).collect(Collectors.toList());
    }

    public <T> List<Container<?>> findContainers(ObjectRequest<T> request) {
        List<Container<?>> matched = new ArrayList<>();
        for (Container<?> c : containers) {
            if (request.getName() != null && !request.isNameAsQualifier() && c.getNames().contains(request.getName())) {
                matched.add(c);
                continue;
            }

            if (request.getType() != null && !request.getType().isAssignableFrom(c.getType())) {
                continue;
            }

            if (request.getName() != null && !c.getNames().contains(request.getName())) {
                continue;
            }

            matched.add(c);
        }

        return pickContainers(request, matched);
    }

    private <T> List<Container<?>> pickContainers(ObjectRequest<T> request, List<Container<?>> containers) {

        // todo: pick by request
        return containers;
    }

    @Override
    public Iterator<Container<?>> iterator() {
        return Collections.unmodifiableCollection(containers).iterator();
    }
}
