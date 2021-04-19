package ahodanenok.di.container;

/**
 * Container for some object of the specified {@link #getObjectClass() class}
 *
 * @param <T> {@link #getObjectClass()}
 */
public interface Container<T> {

    /**
     * Class of the object in a container
     * It's not necessarily its exact class - it also could be one of object's superclasses or interfaces
     */
    Class<T> getObjectClass();

    /**
     * Retrieve an object located in a container
     *
     * todo: nulls as return value is allowable?
     *
     * @throws ahodanenok.di.exception.ObjectRetrievalException
     */
    T getObject();
}
