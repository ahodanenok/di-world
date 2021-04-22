package ahodanenok.di.event;

import ahodanenok.di.container.Container;
import ahodanenok.di.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// todo: priority?
public final class EventHandler {

    private final Container<?> container;
    private final Method method;

    public EventHandler(Container<?> container, Method method) {
        this.container = container;
        this.method = method;
    }

    public boolean handles(Object event) {
        // todo: check if event is supported by the method
        return false;
    }

    public void invoke(Object event) {
        Object instance = container.getObject();
        try {
            ReflectionUtils.invoke(method, instance, event);
        } catch (InvocationTargetException e) {
            // todo: exception + message
            throw new RuntimeException(e);
        }
    }
}
