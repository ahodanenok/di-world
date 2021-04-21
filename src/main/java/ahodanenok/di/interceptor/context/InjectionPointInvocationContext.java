package ahodanenok.di.interceptor.context;

import ahodanenok.di.inject.InjectionPoint;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class InjectionPointInvocationContext implements InvocationContext {

    private final InjectionPoint injectionPoint;
    private final Supplier<Object> dependencyLookup;
    private Map<String, Object> contextData;

    private boolean resolved;
    private Object dependency;

    public InjectionPointInvocationContext(InjectionPoint injectionPoint,
                                           Supplier<Object> dependencyLookup) {
        this.injectionPoint = injectionPoint;
        this.dependencyLookup = dependencyLookup;
    }

    @Override
    public Object getTarget() {
        return null;
    }

    @Override
    public Object getTimer() {
        return null;
    }

    @Override
    public Method getMethod() {
        if (injectionPoint.getTarget() instanceof Method) {
            return (Method) injectionPoint.getTarget();
        } else {
            return null;
        }
    }

    @Override
    public Constructor<?> getConstructor() {
        if (injectionPoint.getTarget() instanceof Constructor<?>) {
            return (Constructor<?>) injectionPoint.getTarget();
        } else {
            return null;
        }
    }

    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public void setParameters(Object[] params) { }

    @Override
    public Map<String, Object> getContextData() {
        if (contextData == null) {
            contextData = new HashMap<>();
        }

        return contextData;
    }

    @Override
    public Object proceed() {
        if (resolved) {
            return dependency;
        }

        dependency = dependencyLookup.get();
        resolved = true;

        return dependency;
    }
}
