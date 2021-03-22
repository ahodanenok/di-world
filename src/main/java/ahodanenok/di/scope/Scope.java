package ahodanenok.di.scope;

import javax.inject.Provider;

public interface Scope<T> {

    T getObject(Provider<T> provider);
}
