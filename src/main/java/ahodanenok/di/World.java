package ahodanenok.di;

import ahodanenok.di.interceptor.Interceptor;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

// todo: inject static methods, own container for this? (StaticConfiguration?)
// todo: instantiate eager objects
// todo: destroying world
// todo: qualifiers
// todo: around invoke

public class World implements Iterable<Container<?>> {

    public static void main(String[] args) {

    }

    private List<Container<?>> containers = new ArrayList<>();
    private EntranceQueue queue = new EntranceQueue(this::register);
    private Map<String, List<InterceptorInvoke>> interceptors = new HashMap<>();

    public EntranceQueue getQueue() {
        return queue;
    }

    private void register(List<ContainerConfiguration<?>> configs) {
        for (ContainerConfiguration<?> config : configs) {
            Container<?> container = buildContainer(config);
            register(container);

            // todo: validate interceptors
            // todo: where to put available types?
            for (Class<?> type : Arrays.asList(AroundConstruct.class, PostConstruct.class, PreDestroy.class, AroundInvoke.class)) {
                Method method = config.getInterceptorMethod(type.getName());
                if (method != null) {
                    interceptors
                            .computeIfAbsent(type.getName(), __ -> new ArrayList<>())
                            .add(new InterceptorInvoke(container, method));
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
        Container<T> container = new Container<>(
                this,
                config.getObjectClass(),
                config.getNames(),
                config.getScope(),
                config.isInterceptor(),
                config.getInterceptors()
        );

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
            // todo: exception+message
            throw new IllegalStateException("not found");
        }
    }

    public <T> List<T> findAll(ObjectRequest<T> request) {
        // todo: suppress unchecked
        return findContainers(request).stream().map(c -> (T) c.getObject()).collect(Collectors.toList());
    }

    public <T> List<Container<?>> findContainers(ObjectRequest<T> request) {
        List<Container<?>> matched = new ArrayList<>();
        for (Container<?> c : containers) {
            if (request.getName() != null && !request.isNameAsQualifier() && c.getNames().contains(request.getName())) {
                matched.add(c);
                continue;
            }

            if (request.getType() != null && !request.getType().isAssignableFrom(c.getObjectClass())) {
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
        // todo: bindings

        List<InterceptorInvoke> typeInterceptors = interceptors.getOrDefault(request.getType(), Collections.emptyList());

        List<Class<?>> interceptorClasses = request.getClasses();
        Map<Interceptor, Integer> byClasses = new HashMap<>();

        List<Interceptor> matchedByDefault = new ArrayList<>();

        for (InterceptorInvoke interceptor : typeInterceptors) {
            if (!request.getClasses().isEmpty()) {
                for (int i = 0; i < interceptorClasses.size(); i++) {
                    Class<?> clazz = request.getClasses().get(i);
                    if (interceptor.getInterceptorClass().isAssignableFrom(clazz)) {
                        byClasses.put(interceptor, i);
                        break;
                    }
                }
            } else if (request.isMatchAll()) {
                matchedByDefault.add(interceptor);
            }
        }

        List<Interceptor> result = new ArrayList<>();
        result.addAll(byClasses.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
        result.addAll(matchedByDefault);

        return new InterceptorChain(result);
    }

    /**
     * Invokes given interceptor method on the object returned by container
     */
    private static class InterceptorInvoke implements Interceptor {

        private final Container<?> container;
        private final Method method;

        public InterceptorInvoke(Container<?> container, Method method) {
            this.container = container;
            this.method = method;
        }

        public Class<?> getInterceptorClass() {
            return container.getObjectClass();
        }

        @Override
        public Object execute(InvocationContext context) throws Exception {
            // todo: create method for invoking methods
            // todo: support interceptor methods with zero parameters
            Object instance = container.getObject();
            return method.invoke(instance, context);
        }
    }

    @Override
    public Iterator<Container<?>> iterator() {
        return Collections.unmodifiableCollection(containers).iterator();
    }
}
