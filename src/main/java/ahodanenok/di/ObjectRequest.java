package ahodanenok.di;

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
    private boolean nameAsQualifier;

    private ObjectRequest() { }

    public ObjectRequest<T> qualifyByName() {
        nameAsQualifier = true;
        return this;
    }

    public boolean isNameAsQualifier() {
        return nameAsQualifier;
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
}
