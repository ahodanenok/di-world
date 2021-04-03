package ahodanenok.di.interceptor;

import javax.interceptor.InvocationContext;

/**
 * Action to be invoked during processing of an {@link InterceptorChain}
 */
public interface Interceptor {

    /**
     * Has the same semantics as {@link InvocationContext#proceed()}
     */
    Object execute(InvocationContext context) throws Exception;
}
