package ahodanenok.di;

import ahodanenok.di.character.Character;
import ahodanenok.di.container.Container;
import ahodanenok.di.container.InjectableContainer;
import ahodanenok.di.container.InterceptorContainer;
import ahodanenok.di.exception.DependencyLookupException;
import ahodanenok.di.interceptor.Interceptor;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

// todo: inject static methods (StaticCharacter?)
// todo: container for user-instantiated objects
// todo: instantiate eager objects
// todo: destroying world + @PreDestroy
// todo: around invoke
// todo: event handlers

public class World implements Iterable<Container<?>> {

    public static void main(String[] args) {

    }

    private List<Container<?>> containers = new ArrayList<>();
    private EntranceQueue queue = new EntranceQueue(this::register);
    private LinkedList<InjectionPoint> injectionPoints = new LinkedList<>();

    public EntranceQueue getQueue() {
        return queue;
    }

    private void register(List<Character<?>> characters) {
        for (Character<?> character : characters) {
            Container<?> container = character.build(this);
            // todo: split containers by collections - injectable, interceptors?
            // todo: is something required to be done before container is added to the world?
            containers.add(container);
        }
    }

    // todo: names are not required to be unique, but should they be checked somehow?
//    private void register(ClassContainer<?> container) {
//        for (ClassContainer<?> c : containers) {
//            for (String n : c.getNames()) {
//                if (container.getNames().contains(n)) {
//                    throw new IllegalStateException(n);
//                }
//            }
//        }
//
//        containers.add(container);
//    }

    @SuppressWarnings("unchecked") // object matched by request will be of type T or its subtype
    public <T> T find(ObjectRequest<T> request) {
        if (request.getType() == InjectionPoint.class) {
            if (injectionPoints.isEmpty()) {
                throw new DependencyLookupException("No active injection point");
            }

            return (T) injectionPoints.getLast();
        }

        List<InjectableContainer<?>> containers = findContainers(request);

        if (containers.size() == 1) {
            return (T) containers.get(0).getObject();
        }

        if (containers.isEmpty() && request.isOptional()) {
            return null;
        }

        if (containers.isEmpty()) {
            throw new DependencyLookupException(String.format(
                    "No dependencies are found for a request '%s'", request));
        }

        // There is a single object without any qualifiers and qualifiers are not specified in request
        if (request.getQualifiers().isEmpty()) {
            List<InjectableContainer<?>> withoutQualifiers = containers.stream()
                    .filter(c -> c.getQualifiers().isEmpty())
                    .collect(Collectors.toList());

            if (withoutQualifiers.size() == 1) {
                return (T) withoutQualifiers.get(0).getObject();
            }
        }

        {
            // There is a single object without any names
            List<InjectableContainer<?>> withoutName = containers.stream()
                    .filter(c -> c.getNames().isEmpty())
                    .collect(Collectors.toList());

            if (withoutName.size() == 1) {
                return (T) withoutName.get(0).getObject();
            }
        }

        {
            // There is a single object with exact type as in request
            List<InjectableContainer<?>> withExactType = containers.stream()
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
        if (request.getType() == InjectionPoint.class) {
            if (injectionPoints.isEmpty()) {
                throw new DependencyLookupException("No active injection point");
            }

            return (List<T>) Collections.singletonList(injectionPoints.getLast());
        }

        return (List<T>) findContainers(request).stream()
                .map(Container::getObject)
                .collect(Collectors.toList());
    }

    // cache resolved containers
    private <T> List<InjectableContainer<?>> findContainers(ObjectRequest<T> request) {
        List<InjectableContainer<?>> matched = new ArrayList<>();

        next:
        for (Container<?> c : containers) {
            if (!(c instanceof InjectableContainer)) {
                continue;
            }

            InjectableContainer<?> injectable = (InjectableContainer<?>) c;

            // The bean has a bean type that matches the required type.
            if (!ReflectionUtils.isAssignable(c.getObjectClass(), request.getType())) {
                continue;
            }

            // The bean has all the required qualifiers.
            for (Annotation qualifier : request.getQualifiers()) {
                if (qualifier instanceof Named) {
                    Named named = (Named) qualifier;
                    if (named.value().isEmpty()) {
                        throw new DependencyLookupException("@Named qualifier must have a value");
                    }

                    if (!injectable.getNames().contains(named.value())) {
                        continue next;
                    }
                } else if (!injectable.getQualifiers().contains(qualifier)) {
                    continue next;
                }
            }

            matched.add(injectable);
        }

        return matched;
    }

    public void pushInjectionPoint(InjectionPoint injectionPoint) {
        if (injectionPoint == null) {
            throw new IllegalArgumentException("Injection point is null");
        }

        injectionPoints.addLast(injectionPoint);
    }

    public void popInjectionPoint() {
        if (injectionPoints.isEmpty()) {
            throw new IllegalStateException("No active injection point");
        }

        injectionPoints.removeLast();
    }

    // todo: cache resolved interceptors
    public InterceptorChain getInterceptorChain(InterceptorRequest request) {
        List<Interceptor> result = new ArrayList<>();

        if (!request.getClasses().isEmpty()) {
            // interceptors are declared explicitly
            for (Class<?> interceptorClass : request.getClasses()) {
                for (Container<?> container : containers) {
                    if (container instanceof InterceptorContainer<?>
                            && interceptorClass == container.getObjectClass()) {
                        Interceptor interceptor = ((InterceptorContainer<?>) container).getInterceptor(request.getType());
                        if (interceptor != null) {
                            result.add(interceptor);
                        }
                    }
                }
            }
        } else if (!request.getBindings().isEmpty()) {
           for (Container<?> container : containers) {
               if (container instanceof InterceptorContainer<?>) {
                   InterceptorContainer<?> interceptorContainer = (InterceptorContainer<?>) container;

                   // JSR-318 (Interceptors 1.2), 3.4
                   // An interceptor is bound to a method or constructor if:
                   // - The method or constructor has all the interceptor bindings of the interceptor.
                   if (interceptorContainer.getInterceptorBindings().size() == request.getBindings().size()
                           && interceptorContainer.getInterceptorBindings().containsAll(request.getBindings())) {
                       Interceptor interceptor = ((InterceptorContainer<?>) container).getInterceptor(request.getType());
                       // - The interceptor intercepts the given kind of lifecycle event or method
                       if (interceptor != null) {
                           result.add(interceptor);
                       }
                   }
               }
           }
        } else if (request.isMatchAll()) {
            // all of a type
            for (Container<?> container : containers) {
                if (!(container instanceof InterceptorContainer<?>)) {
                    continue;
                }

                Interceptor interceptor = ((InterceptorContainer<?>) container).getInterceptor(request.getType());
                if (interceptor != null) {
                    result.add(interceptor);
                }
            }
        }
        // else empty chain

        return new InterceptorChain(result);
    }

    @Override
    public Iterator<Container<?>> iterator() {
        return Collections.unmodifiableCollection(containers).iterator();
    }
}
