package ahodanenok.di.container;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * Container which provides objects for injecting into other objects as dependencies
 */
public interface InjectableContainer<T> extends Container<T> {

    /**
     * Object names
     * @see javax.inject.Named
     */
    Set<String> getNames();

    /**
     * Object qualifiers
     * @see javax.inject.Qualifier
     */
    List<Annotation> getQualifiers();
}
