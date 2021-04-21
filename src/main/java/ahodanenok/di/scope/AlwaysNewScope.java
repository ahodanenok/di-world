package ahodanenok.di.scope;

import javax.inject.Provider;

/**
 * Instantiates object on every request
 */
public class AlwaysNewScope<T> implements Scope<T> {

    private static final AlwaysNewScope<?> INSTANCE = new AlwaysNewScope<>();

    // method is a helper to avoid doing casts in client code
    @SuppressWarnings("unchecked") // the scope doesn't care about type, returns what provider gave
    public static <T> AlwaysNewScope<T> getInstance() {
        return (AlwaysNewScope<T>) INSTANCE;
    }

    @Override
    public T getObject(Provider<T> provider) {
        return provider.get();
    }
}
