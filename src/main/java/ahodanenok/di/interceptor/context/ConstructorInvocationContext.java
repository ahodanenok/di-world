package ahodanenok.di.interceptor.context;

import ahodanenok.di.util.ReflectionUtils;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Invocation context for AroundConstruct interceptors.
 *
 * Before the first interceptor is invoked, constructor arguments
 * have been already resolved and are available via {@link #getParameters} method.
 */
public class ConstructorInvocationContext implements InvocationContext {

    private Object target;
    private final Constructor<?> constructor;
    private Object[] parameters;
    private Map<String, Object> contextData;

    public ConstructorInvocationContext(Constructor<?> constructor) {
        this.constructor = constructor;
        this.parameters = new Object[constructor.getParameterCount()];
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Object getTimer() {
        return null;
    }

    @Override
    public Method getMethod() {
        return null;
    }

    @Override
    public Constructor<?> getConstructor() {
        return constructor;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(Object[] params) {
        ReflectionUtils.validateParameters(constructor, params);
        this.parameters = params;
    }

    @Override
    public Map<String, Object> getContextData() {
        if (contextData == null) {
            contextData = new HashMap<>();
        }

        return contextData;
    }

    @Override
    public Object proceed() throws Exception {
        if (target != null) {
            return target;
        }

        // todo: accessible
        constructor.setAccessible(true);

        target = constructor.newInstance(parameters);
        return target;
    }
}
