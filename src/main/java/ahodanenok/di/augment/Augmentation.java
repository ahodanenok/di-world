package ahodanenok.di.augment;

import ahodanenok.di.character.Character;

import java.lang.reflect.Constructor;

/**
 * Augment object produced by container
 */
public interface Augmentation {

    Constructor<?> augmentBeforeInstantiated(Character<?> character, Constructor<?> constructor);

    Object augmentAfterInstantiated(Character<?> character, Object instance);

    Object augmentAfterInjected(Character<?> character, Object instance);

    Object augmentAfterConstructed(Character<?> character, Object instance);
}
