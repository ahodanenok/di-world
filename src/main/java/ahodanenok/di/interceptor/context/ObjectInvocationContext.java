package ahodanenok.di.interceptor.context;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * InvocationContext for a lifecycle interceptors
 * when there is no lifecycle method declared on the target class.
 *
 * Proceed is a no-op operation and null is returned.
 */
public class ObjectInvocationContext implements InvocationContext {

    private final Object object;
    private Object[] parameters;
    private Map<String, Object> contextData;

    public ObjectInvocationContext(Object object) {
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
        if (parameters == null) {
            parameters = new Object[0];
        }

        // must return parameters set by setParameters if we don't throw an exception there
        return parameters;
    }

    @Override
    public void setParameters(Object[] params) {
        // method must throw an IllegalStateException if params don't match
        // don't throw because can't say that any parameter doesn't match - vacuous truth
        this.parameters = params != null ? params : new Object[0];
    }

    @Override
    public Map<String, Object> getContextData() {
        if (contextData == null) {
            contextData = new HashMap<>();
        }

        return contextData;
    }

    @Override
    public Object proceed() {
        return null;
    }
}
