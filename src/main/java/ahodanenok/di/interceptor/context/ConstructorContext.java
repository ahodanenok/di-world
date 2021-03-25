package ahodanenok.di.interceptor.context;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public class ConstructorContext implements InvocationContext {

    private Object target;
    private Constructor<?> constructor;
    private Object[] parameters;

    public ConstructorContext(Constructor<?> constructor) {
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
        // todo: check params are valid
        this.parameters = params;
    }

    @Override
    public Map<String, Object> getContextData() {
        return Collections.emptyMap();
    }

    @Override
    public Object proceed() throws Exception {
        if (target != null) {
            return target;
        }

        // todo: accessible
        target = constructor.newInstance(parameters);
        return target;
    }
}
