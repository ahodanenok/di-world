package ahodanenok.di;

import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class World implements Iterable<Container<?>> {

    public static void main(String[] args) {

    }

    private List<Container<?>> containers = new ArrayList<>();
    private EntranceQueue queue = new EntranceQueue(this::register);
    private Map<String, List<Method>> interceptors = new HashMap<>();

    public EntranceQueue getQueue() {
        return queue;
    }

    private void register(List<ContainerConfiguration<?>> configs) {
        for (ContainerConfiguration<?> config : configs) {
            register(buildContainer(config));

            // todo: validate interceptors
            if (config.getDeclaredInterceptors() != null) {
                for (Map.Entry<String, List<Method>> entry : config.getDeclaredInterceptors().entrySet()) {
                    interceptors.computeIfAbsent(entry.getKey(), __ -> new ArrayList<>()).addAll(entry.getValue());
                }
            }
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

    private <T> Container<T> buildContainer(ContainerConfiguration<T> config) {
        // todo: configuration class per container type (class, factory method, instance)
        // todo: configuration instantiates container of the appropriate type and later world is bound - c.bind(world)
        Container<T> container = new Container<>(this, config.getType(), config.getNames(), config.getScope());

        return container;
    }

    public <T> T find(ObjectRequest<T> request) {
        List<Container<?>> containers = findContainers(request);
        if (containers.size() == 1) {
            // todo: supress unchecked
            return (T) containers.get(0).getObject();
        } else if (containers.size() > 1){
            // todo: exception+message
            throw new IllegalStateException("multiple");
        } else {
            throw new IllegalStateException("not found");
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

    public InterceptorChain getInterceptorChain(InterceptorRequest request) {
        // todo: handle request criteria
        return new InterceptorChain(interceptors.getOrDefault(request.getType(), Collections.emptyList()));
    }

    @Override
    public Iterator<Container<?>> iterator() {
        return Collections.unmodifiableCollection(containers).iterator();
    }
}
