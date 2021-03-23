package ahodanenok.di.interceptor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class InterceptorChain {

    private List<Method> methods;

    public InterceptorChain(List<Method> methods) {
        this.methods = methods;
    }

    public List<Method> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public int size() {
        return methods.size();
    }
}
