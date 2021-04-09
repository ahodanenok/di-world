package ahodanenok.di;

import java.lang.annotation.Annotation;
import java.util.List;

public class ObjectRequest<T> {

    public static <T> ObjectRequest<T> byName(String name) {
        ObjectRequest<T> request = new ObjectRequest<>();
        request.withName(name);
        return request;
    }

    public static <T> ObjectRequest<T> byType(Class<T> type) {
        ObjectRequest<T> request = new ObjectRequest<T>();
        request.withType(type);
        return request;
    }

    private Class<?> type;
    private String name;
    private List<Annotation> qualifiers;

    private ObjectRequest() { }

    public ObjectRequest<T> qualifyByName() {
        return this;
    }

    public Class<?> getType() {
        return type;
    }

    public <C extends T> ObjectRequest<T> withType(Class<C> type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public ObjectRequest<T> withName(String name) {
        this.name = name;
        return this;
    }

    public ObjectRequest<T> withQualifiers(List<Annotation> qualifiers) {
        this.qualifiers = qualifiers;
        return this;
    }

    public List<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public String toString() {
        return String.format("ObjectRequest(type=%s, name=%s, qualifiers=%s)", type, name, qualifiers);
    }
}
