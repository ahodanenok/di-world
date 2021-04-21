package ahodanenok.di;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Request for retrieving objects from the world by type and qualifiers
 */
public class ObjectRequest<T> {

    /**
     * Type of objects we're interested in, could be exact type or any of its superclasses or interfaces
     */
    public static <T> ObjectRequest<T> of(Class<T> type) {
        ObjectRequest<T> request = new ObjectRequest<>();
        request.type = type;
        return request;
    }

    private Class<?> type;
    private List<Annotation> qualifiers;
    private boolean optional;

    private ObjectRequest() { }

    public Class<?> getType() {
        return type;
    }

    /**
     * @see javax.inject.Qualifier
     */
    public ObjectRequest<T> withQualifiers(List<Annotation> qualifiers) {
        this.qualifiers = new ArrayList<>(qualifiers);
        return this;
    }

    public List<Annotation> getQualifiers() {
        if (qualifiers != null) {
            return Collections.unmodifiableList(qualifiers);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Mark request as optional, meaning that if no objects are found for the request, it's ok
     */
    public ObjectRequest<T> optional() {
        this.optional = true;
        return this;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return String.format("ObjectRequest(type=%s, qualifiers=%s)", type, qualifiers);
    }
}
