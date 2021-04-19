package ahodanenok.di.container;

import ahodanenok.di.interceptor.Interceptor;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Container for objects acting as interceptors in the world
 */
public interface InterceptorContainer<T> extends Container<T> {

    /**
     * Interceptor bindings of the interceptor in a container
     * @see javax.interceptor.InterceptorBinding
     */
    List<Annotation> getInterceptorBindings();

    /**
     * Interceptor method for a particular type
     *
     * @param type interceptor type
     * @return interceptor or null if object doesn't declare such interceptor method
     * @see javax.interceptor.AroundConstruct
     * @see javax.interceptor.AroundInvoke
     * @see javax.annotation.PreDestroy
     * @see javax.annotation.PostConstruct
     * todo: add abstraction over a string, it's not clear how to pass i.e AroundConstruct or other type here
     */
    Interceptor getInterceptor(String type);
}
