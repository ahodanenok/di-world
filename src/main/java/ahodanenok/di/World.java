package ahodanenok.di;

import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.container.ClassContainer;
import ahodanenok.di.exception.DependencyLookupException;
import ahodanenok.di.interceptor.Interceptor;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

// todo: inject static methods, own container for this? (StaticConfiguration?)
// todo: instantiate eager objects
// todo: destroying world
// todo: around invoke

public class World implements Iterable<ClassContainer<?>> {

    public static void main(String[] args) {

    }

    private List<ClassContainer<?>> containers = new ArrayList<>();
    private EntranceQueue queue = new EntranceQueue(this::register);
    private Map<String, List<InterceptorInvoke>> interceptors = new HashMap<>();

    public EntranceQueue getQueue() {
        return queue;
    }

    private void register(List<ClassCharacter<?>> characters) {
        for (ClassCharacter<?> character : characters) {
            ClassContainer<?> container = buildContainer(character);
            register(container);

            // todo: validate interceptors
            // todo: where to put available types?
            for (Class<?> type : Arrays.asList(AroundConstruct.class, PostConstruct.class, PreDestroy.class, AroundInvoke.class)) {
                Method method = character.getInterceptorMethod(type.getName());
                if (method != null) {
                    interceptors
                            .computeIfAbsent(type.getName(), __ -> new ArrayList<>())
                            .add(new InterceptorInvoke(container, method));
                }
            }
        }
    }

    private void register(ClassContainer<?> container) {
        for (ClassContainer<?> c : containers) {
            for (String n : c.getNames()) {
                if (container.getNames().contains(n)) {
                    throw new IllegalStateException(n);
                }
            }
        }

        containers.add(container);
    }

    private <T> ClassContainer<T> buildContainer(ClassCharacter<T> character) {
        // todo: configuration class per container type (class, factory method, instance)
        // todo: configuration instantiates container of the appropriate type and later world is bound - c.bind(world)
        ClassContainer<T> container = new ClassContainer<>(this,  character);

        return container;
    }

    public <T> T find(ObjectRequest<T> request) {
        List<ClassContainer<?>> containers = findContainers(request);
        if (containers.size() == 1) {
            // todo: suppress unchecked
            return (T) containers.get(0).getObject();
        } else if (containers.size() > 1){
            throw new DependencyLookupException(String.format(
                    "Multiple matching dependencies are found for a request '%s': %s",
                    request,
                    containers.stream().map(c -> c.getObjectClass().getName()).collect(Collectors.toList())));
        } else {
            throw new DependencyLookupException(String.format("No dependencies are found for a request '%s'", request));
        }
    }

    public <T> List<T> findAll(ObjectRequest<T> request) {
        // todo: suppress unchecked
        return findContainers(request).stream().map(c -> (T) c.getObject()).collect(Collectors.toList());
    }

    public <T> List<ClassContainer<?>> findContainers(ObjectRequest<T> request) {

        List<ClassContainer<?>> matched = new ArrayList<>();
        for (ClassContainer<?> c : containers) {
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

    private <T> List<ClassContainer<?>> pickContainers(ObjectRequest<T> request, List<ClassContainer<?>> containers) {
        List<ClassContainer<?>> matched = new ArrayList<>();

        if (request.getQualifiers() != null && !request.getQualifiers().isEmpty()) {
            List<Annotation> requestQualifiers = request.getQualifiers();
            for (ClassContainer<?> c : containers) {
                List<Annotation> containerQualifiers = c.getQualifiers();
                if (containerQualifiers.containsAll(requestQualifiers)) {
                    matched.add(c);
                }
            }
        } else {
            matched = containers;
        }

        return matched;
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

        private final ClassContainer<?> container;
        private final Method method;

        public InterceptorInvoke(ClassContainer<?> container, Method method) {
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
    public Iterator<ClassContainer<?>> iterator() {
        return Collections.unmodifiableCollection(containers).iterator();
    }
}
