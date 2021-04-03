package ahodanenok.di.interceptor;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Sequence of actions to be invoked before calling {@link InvocationContext#proceed()} of the given context.
 */
public class InterceptorChain {

    private final List<Interceptor> interceptors;

    /**
     * Constructs an empty chain
     */
    public InterceptorChain() {
        this(Collections.emptyList());
    }

    /**
     * Constructs chain with the given interceptors
     */
    public InterceptorChain(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * Get unmodifiable list of interceptors in the chain
     */
    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    /**
     * Number of interceptors in the chain
     * todo: is this method needed?
     */
    public int length() {
        return interceptors.size();
    }

    /**
     * Execute chain of interceptors
     *
     * Given context will be executed only if every interceptor in the chain
     * calls {@link InvocationContext#proceed()} of their parameter
     *
     * If the chain is empty, context will be invoked immediately
     */
    public Object invoke(InvocationContext context) throws Exception {
        return new Execution(context).proceed();
    }

    private class Execution implements InvocationContext {

        private int pos;
        private final InvocationContext context;

        public Execution(InvocationContext context) {
            this.context = context;
        }

        @Override
        public Object getTarget() {
            return context.getTarget();
        }

        @Override
        public Object getTimer() {
            return context.getTimer();
        }

        @Override
        public Method getMethod() {
            return context.getMethod();
        }

        @Override
        public Constructor<?> getConstructor() {
            return context.getConstructor();
        }

        @Override
        public Object[] getParameters() {
            return context.getParameters();
        }

        @Override
        public void setParameters(Object[] params) {
            context.setParameters(params);
        }

        @Override
        public Map<String, Object> getContextData() {
            return context.getContextData();
        }

        @Override
        public Object proceed() throws Exception {
            if (pos < interceptors.size()) {
                return interceptors.get(pos++).execute(this);
            } else {
                return context.proceed();
            }
        }
    }
}
