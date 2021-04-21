package ahodanenok.di.interceptor;

import ahodanenok.di.container.Container;
import ahodanenok.di.util.ReflectionUtils;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * Invokes given interceptor method on the object returned by container

 */
public class InterceptorInvoke implements Interceptor {

    private final Container<?> container;
    private final Method method;

    public InterceptorInvoke(Container<?> container, Method method) {
        this.container = container;
        this.method = method;
    }

    @Override
    public Object execute(InvocationContext context) throws Exception {
        // todo: support interceptor methods with zero parameters
        Object instance = container.getObject();
        return ReflectionUtils.invoke(method, instance, context);
    }
}
