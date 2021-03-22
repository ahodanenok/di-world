package ahodanenok.di.scope;

import javax.inject.Provider;

public class SingletonScope<T> implements Scope<T> {

    private T instance;

    @Override
    public T getObject(Provider<T> provider) {
        if (instance == null) {
            instance = provider.get();
            // todo: if null again?
        }

        return instance;
    }
}
