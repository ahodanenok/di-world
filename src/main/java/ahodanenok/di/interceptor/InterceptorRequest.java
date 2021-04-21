package ahodanenok.di.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Request for retrieving {@link InterceptorChain}
 * @see ahodanenok.di.World#getInterceptorChain(InterceptorRequest)
 */
public final class InterceptorRequest {

    /**
     * Request for interceptor chain of given type
     */
    public static InterceptorRequest of(String type) {
        return new InterceptorRequest(type);
    }

    private final String type;
    private List<Class<?>> classes;
    private List<Annotation> bindings;
    private boolean matchAll;

    /**
     * Type of intercepted event
     */
    private InterceptorRequest(String type) {
        this.type = type;
    }

    /**
     * Interceptors to look for interceptor methods
     */
    public InterceptorRequest withClasses(List<Class<?>> classes) {
        this.classes = new ArrayList<>(classes);
        return this;
    }

    /**
     * Type of intercepted event
     */
    public String getType() {
        return type;
    }

    /**
     * Interceptors to look for interceptor methods
     */
    public List<Class<?>> getClasses() {
        if (classes != null) {
            return Collections.unmodifiableList(classes);
        } else {
            return Collections.emptyList();
        }
    }

    public InterceptorRequest withBindings(List<Annotation> bindings) {
        this.bindings = new ArrayList<>(bindings);
        return this;
    }

    public List<Annotation> getBindings() {
        if (bindings != null) {
            return Collections.unmodifiableList(bindings);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Match all interceptors of type
     * If this flag is set, other criteria is ignored
     */
    public InterceptorRequest matchAll() {
        this.matchAll = true;
        return this;
    }

    public boolean isMatchAll() {
        return matchAll;
    }
}
