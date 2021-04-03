package ahodanenok.di.interceptor.context;

import ahodanenok.di.util.ReflectionUtils;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodInvocationContext implements InvocationContext {

    private final Object object;
    private final Method method;
    private Object[] parameters;
    private Map<String, Object> contextData;

    public MethodInvocationContext(Object object, Method method) {
        this.object = object;
        this.method = method;
        this.parameters = new Object[method.getParameterCount()];
    }

    @Override
    public Object getTarget() {
        return object;
    }

    @Override
    public Object getTimer() {
        return null;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Constructor<?> getConstructor() {
        return null;
    }

    @Override
    public Object[] getParameters() {
        // todo: copy
        return parameters;
    }

    @Override
    public void setParameters(Object[] params) {
        ReflectionUtils.validateParameters(method, params);
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
        // todo: make accessible only if needed
        method.setAccessible(true);
        return method.invoke(object, parameters);
    }
}
