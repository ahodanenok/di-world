package ahodanenok.di;

import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.container.ClassContainer;
import ahodanenok.di.exception.DependencyLookupException;
import ahodanenok.di.interceptor.Interceptor;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
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

    @SuppressWarnings("unchecked") // object matched by request will be of type T or its subtype
    public <T> T find(ObjectRequest<T> request) {
        List<ClassContainer<?>> containers = findContainers(request);

        if (containers.size() == 1) {
            return (T) containers.get(0).getObject();
        }

        if (containers.isEmpty()) {
            throw new DependencyLookupException(String.format(
                    "No dependencies are found for a request '%s'", request));
        }

        // There is a single object without any qualifiers and qualifiers are not specified in request
        if (request.getQualifiers().isEmpty()) {
            List<ClassContainer<?>> withoutQualifiers = containers.stream()
                    .filter(c -> c.getQualifiers().isEmpty())
                    .collect(Collectors.toList());

            if (withoutQualifiers.size() == 1) {
                return (T) withoutQualifiers.get(0).getObject();
            }
        }

        {
            // There is a single object without any names
            List<ClassContainer<?>> withoutName = containers.stream()
                    .filter(c -> c.getNames().isEmpty())
                    .collect(Collectors.toList());

            if (withoutName.size() == 1) {
                return (T) withoutName.get(0).getObject();
            }
        }

        {
            // There is a single object with exact type as in request
            List<ClassContainer<?>> withExactType = containers.stream()
                    .filter(c -> c.getObjectClass() == request.getType())
                    .collect(Collectors.toList());

            if (withExactType.size() == 1) {
                return (T) withExactType.get(0).getObject();
            }
        }

        // None matched...
        throw new DependencyLookupException(String.format(
                "Multiple matching dependencies are found for a request '%s': %s",
                request,
                containers.stream().map(c -> c.getObjectClass().getName()).collect(Collectors.toList())));
    }

    @SuppressWarnings("unchecked") // all objects matched by request will be of type T or its subtype
    public <T> List<T> findAll(ObjectRequest<T> request) {
        return (List<T>) findContainers(request).stream()
                .map(ClassContainer::getObject)
                .collect(Collectors.toList());
    }

    public <T> List<ClassContainer<?>> findContainers(ObjectRequest<T> request) {
        List<ClassContainer<?>> matched = new ArrayList<>();

        next:
        for (ClassContainer<?> c : containers) {
            // The bean has a bean type that matches the required type.
            if (!ReflectionUtils.isAssignable(c.getObjectClass(), request.getType())) {
                continue;
            }

            // Use name as a qualifier
//            if (request.getName() != null && !c.getNames().contains(request.getName())) {
//                continue;
//            }

            // The bean has all the required qualifiers.
            for (Annotation qualifier : request.getQualifiers()) {
                if (qualifier instanceof Named) {
                    Named named = (Named) qualifier;
                    if (named.value().isEmpty()) {
                        throw new DependencyLookupException("@Named qualifier must have a value");
                    }

                    if (!c.getNames().contains(named.value())) {
                        continue next;
                    }
                } else if (!c.getQualifiers().contains(qualifier)) {
                    continue next;
                }
            }

            matched.add(c);
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
