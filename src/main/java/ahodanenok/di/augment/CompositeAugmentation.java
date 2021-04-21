package ahodanenok.di.augment;

import ahodanenok.di.character.Character;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Represents list of augmentations as a single augmentation
 */
public class CompositeAugmentation implements Augmentation {

    private final List<Augmentation> augmentations;

    public CompositeAugmentation(List<Augmentation> augmentations) {
        this.augmentations = augmentations;
    }

    @Override
    public Constructor<?> augmentBeforeInstantiated(Character<?> character, Constructor<?> constructor) {
        Constructor<?> current = constructor;
        for (Augmentation a : augmentations) {
            current = a.augmentBeforeInstantiated(character, current);
        }

        return current;
    }

    @Override
    public Object augmentAfterInstantiated(Character<?> character, Object instance) {
        Object current = instance;
        for (Augmentation a : augmentations) {
            current = a.augmentAfterInstantiated(character, current);
        }

        return current;
    }

    @Override
    public Object augmentAfterInjected(Character<?> character, Object instance) {
        Object current = instance;
        for (Augmentation a : augmentations) {
            current = a.augmentAfterInjected(character, current);
        }

        return current;
    }

    @Override
    public Object augmentAfterConstructed(Character<?> character, Object instance) {
        Object current = instance;
        for (Augmentation a : augmentations) {
            current = a.augmentAfterConstructed(character, current);
        }

        return current;
    }
}
