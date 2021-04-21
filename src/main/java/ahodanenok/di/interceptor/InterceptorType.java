package ahodanenok.di.interceptor;

import ahodanenok.di.inject.AroundInject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import java.lang.annotation.Annotation;

public final class InterceptorType {

    public static final InterceptorType POST_CONSTRUCT = InterceptorType.of(PostConstruct.class);
    public static final InterceptorType PRE_DESTROY = InterceptorType.of(PreDestroy.class);
    public static final InterceptorType AROUND_CONSTRUCT = InterceptorType.of(AroundConstruct.class);
    public static final InterceptorType AROUND_INVOKE = InterceptorType.of(AroundInvoke.class);
    public static final InterceptorType AROUND_INJECT = InterceptorType.of(AroundInject.class);

    public static InterceptorType of(Class<? extends Annotation> type) {
        return new InterceptorType(type.getName());
    }

    private final String value;

    public InterceptorType(String value) {
        this.value = value;
    }

    public boolean matches(Annotation annotation) {
        return annotation != null && annotation.annotationType().getName().equals(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof InterceptorType)) {
            return false;
        }

        InterceptorType other = (InterceptorType) obj;
        return value.equals(other.value);
    }

    @Override
    public String toString() {
        return "InterceptorType(" + value + ")";
    }
}
