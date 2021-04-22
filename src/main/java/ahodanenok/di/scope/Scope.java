package ahodanenok.di.scope;

import javax.inject.Provider;

public interface Scope<T> {

    T getObject(Provider<T> provider);

    default void destroy() {
        // todo: should scope be marked after destroy, so it can't be used anymore
        // no-op
    }
}
