package ahodanenok.di.scope;

import javax.inject.Provider;

/**
 * Instantiates object at first retrieval and returns this instance on all subsequent requests
 */
public class SingletonScope<T> implements Scope<T> {

    private T instance;

    @Override
    public T getObject(Provider<T> provider) {
        if (instance == null) {
            instance = provider.get();
            if (instance == null) {
                throw new IllegalStateException("Provider returned null, that's not appropriate!");
            }
        }

        return instance;
    }
}
