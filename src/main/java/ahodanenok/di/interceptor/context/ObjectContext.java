package ahodanenok.di.interceptor.context;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public class ObjectContext implements InvocationContext {

    private Object object;

    public ObjectContext(Object object) {
        this.object = object;
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
        return null;
    }

    @Override
    public Constructor<?> getConstructor() {
        return null;
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public void setParameters(Object[] params) {
        // no-op
    }

    @Override
    public Map<String, Object> getContextData() {
        return Collections.emptyMap();
    }

    @Override
    public Object proceed() throws Exception {
        return null;
    }
}
