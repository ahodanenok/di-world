package ahodanenok.di.scope;

import javax.inject.Provider;

public class AlwaysNewScope<T> implements Scope<T> {

    private static final AlwaysNewScope<?> INSTANCE = new AlwaysNewScope<>();

    @SuppressWarnings("unchecked")
    public static <T> AlwaysNewScope<T> getInstance() {
        return (AlwaysNewScope<T>) INSTANCE;
    }

    @Override
    public T getObject(Provider<T> provider) {
        return provider.get();
    }
}
