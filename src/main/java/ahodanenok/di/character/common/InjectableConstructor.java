package ahodanenok.di.character.common;

import ahodanenok.di.exception.CharacterMetadataException;
import ahodanenok.di.metadata.ExecutableMetadataReader;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructor used to instantiate objects of the given class in the world
 * @param <T> class with the constructor
 */
public final class InjectableConstructor<T> {

    private final Class<T> clazz;
    private Constructor<T> constructor;

    public InjectableConstructor(Class<T> clazz) {
        if (clazz == null) {
            throw new CharacterMetadataException("Class can't be null");
        }

        this.clazz = clazz;
    }

    public void set(Constructor<?> constructor) {
        if (constructor == null) {
            throw new CharacterMetadataException("Constructor can't be null");
        }

        if (constructor.getDeclaringClass() != clazz) {
            throw new CharacterMetadataException(String.format(
                    "Constructor doesn't belong to class '%s'", clazz));
        }

        @SuppressWarnings("unchecked") // constructor will be from the given class
        Constructor<T> c = (Constructor<T>) constructor;
        this.constructor = c;
    }

    public Constructor<T> get() {
        if (constructor != null) {
            return constructor;
        }

        @SuppressWarnings("unchecked") // constructor will be from the given class
        Constructor<T> c = (Constructor<T>) resolve();
        this.constructor = c;

        return constructor;
    }

    private Constructor<?> resolve() {
        List<Constructor<?>> constructors = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            ExecutableMetadataReader metadataReader = new ExecutableMetadataReader(constructor);
            if (metadataReader.readInjectable()) {
                constructors.add(constructor);
            }
        }

        if (constructors.size() == 1) {
            return constructors.get(0);
        } else if (constructors.size() > 1) {
            throw new CharacterMetadataException(String.format(
                    "Only one constructor can be injectable, injectable constructors in '%s' are: %s",
                    clazz, constructors));
        }

        Constructor<?>[] publicConstructors = clazz.getConstructors();
        // If there a single public constructor, using it
        if (publicConstructors.length == 1) {
            return publicConstructors[0];
        }

        try {
            // falling back to no-arg public constructor
            return clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new CharacterMetadataException(String.format(
                    "Couldn't resolve constructor for '%s', provide it explicitly in a character" +
                            " or use @Inject annotation to mark which constructor to use", clazz));
        }
    }
}
