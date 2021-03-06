package ahodanenok.di;

import ahodanenok.di.augment.Augmentation;
import ahodanenok.di.augment.CompositeAugmentation;
import ahodanenok.di.character.Character;
import ahodanenok.di.container.Container;
import ahodanenok.di.container.EventHandlerContainer;
import ahodanenok.di.container.InjectableContainer;
import ahodanenok.di.container.InterceptorContainer;
import ahodanenok.di.event.EventHandler;
import ahodanenok.di.exception.DependencyLookupException;
import ahodanenok.di.inject.InjectionPoint;
import ahodanenok.di.interceptor.Interceptor;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;
import ahodanenok.di.queue.EntranceQueue;
import ahodanenok.di.util.ReflectionUtils;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

// todo: container for user-instantiated objects
// todo: instantiate eager objects
// todo: logging
public final class DefaultWorld implements WorldInternals, World {

    private final List<Container<?>> containers = new ArrayList<>();
    private final EntranceQueue queue = new EntranceQueue(this::register);
    private final LinkedList<InjectionPoint> injectionPoints = new LinkedList<>();
    private final List<Augmentation> augmentations = new ArrayList<>();

    public DefaultWorld() {
        this.augmentations.add(new ObjectsAugmentation());
    }

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

    @Override
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

    @Override
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

    // todo: lookup by name
    // todo: cache resolved containers
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
    public void installAugmentation(Augmentation augmentation) {
        if (augmentation == null) {
            throw new IllegalArgumentException("Augmentation is null");
        }

        this.augmentations.add(augmentation);
    }

    public Augmentation requestAugmentation() {
        return new CompositeAugmentation(augmentations);
    }

    @Override
    public Iterator<Container<?>> iterator() {
        return Collections.unmodifiableCollection(containers).iterator();
    }

    @Override
    public void fireEvent(Object event) {
        List<EventHandler> handlers = new ArrayList<>();
        for (Container<?> container : containers) {
            if (!(container instanceof EventHandlerContainer<?>)) {
                continue;
            }

            EventHandlerContainer<?> ehc = (EventHandlerContainer<?>) container;
            // todo: retrieve event handlers during registration to avoid iterating over all containers
            for (EventHandler h : ehc.getEventHandlers()) {
                if (h.handles(event)) {
                    handlers.add(h);
                }
            }
        }

        // todo: sort by priority?
        for (EventHandler h : handlers) {
            h.invoke(event);
        }
    }

    @Override
    public void destroy() {
        // todo: destroy order?
        for (Container<?> container : containers) {
            try {
                container.destroy();
            } catch (Exception e) {
                // todo: log error
                e.printStackTrace();
                // allow other containers to be destroyed
            }
        }
    }

    private class ObjectsAugmentation implements Augmentation {
        @Override
        public Object augmentAfterConstructed(Character<?> character, Object instance) {
            // todo: proxy for around invoke
            return instance;
        }
    }
}
