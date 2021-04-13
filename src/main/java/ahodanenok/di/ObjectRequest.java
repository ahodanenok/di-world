package ahodanenok.di;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class ObjectRequest<T> {

    public static <T> ObjectRequest<T> of(Class<T> type) {
        ObjectRequest<T> request = new ObjectRequest<T>();
        request.withType(type);
        return request;
    }

    private Object context;
    private Class<?> type;
    private List<Annotation> qualifiers;

    private ObjectRequest() { }

    public ObjectRequest<T> withContext(Object context) {
        this.context = context;
        return this;
    }

    public Object getContext() {
        return context;
    }

    public Class<?> getType() {
        return type;
    }

    public <C extends T> ObjectRequest<T> withType(Class<C> type) {
        this.type = type;
        return this;
    }

    public ObjectRequest<T> withQualifiers(List<Annotation> qualifiers) {
        this.qualifiers = qualifiers;
        return this;
    }

    public List<Annotation> getQualifiers() {
        return qualifiers != null ? qualifiers : Collections.emptyList();
    }

    @Override
    public String toString() {
        return String.format(
                "ObjectRequest(type=%s, qualifiers=%s, context=%s)",
                type, qualifiers, context);
    }
}
