package ahodanenok.di.augment;

import ahodanenok.di.character.Character;

import java.lang.reflect.Constructor;

/**
 * Augment object produced by container
 */
public interface Augmentation {

    default Constructor<?> augmentBeforeInstantiated(Character<?> character, Constructor<?> constructor) {
        return constructor;
    }

    default Object augmentAfterInstantiated(Character<?> character, Object instance) {
        return instance;
    }

    default Object augmentAfterInjected(Character<?> character, Object instance) {
        return instance;
    }

    default Object augmentAfterConstructed(Character<?> character, Object instance) {
        return instance;
    }
}
