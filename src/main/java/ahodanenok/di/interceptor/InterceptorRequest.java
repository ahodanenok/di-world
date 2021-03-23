package ahodanenok.di.interceptor;

public final class InterceptorRequest {

    public static InterceptorRequest ofType(String type) {
        return new InterceptorRequest(type);
    }

    private String type;

    private InterceptorRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
