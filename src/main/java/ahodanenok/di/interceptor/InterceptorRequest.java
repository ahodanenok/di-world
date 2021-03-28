package ahodanenok.di.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InterceptorRequest {

    public static InterceptorRequest ofType(String type) {
        return new InterceptorRequest(type);
    }

    private String type;
    private List<Class<?>> classes;
    private boolean matchAll;

    private InterceptorRequest(String type) {
        this.type = type;
    }

    public InterceptorRequest withClasses(List<Class<?>> classes) {
        if (this.classes == null) {
            this.classes = new ArrayList<>();
        }

        this.classes.addAll(classes);
        return this;
    }

    public String getType() {
        return type;
    }

    public List<Class<?>> getClasses() {
        return classes != null ? classes : Collections.emptyList();
    }

    public InterceptorRequest matchAll() {
        this.matchAll = true;
        return this;
    }

    public boolean isMatchAll() {
        return matchAll;
    }
}
