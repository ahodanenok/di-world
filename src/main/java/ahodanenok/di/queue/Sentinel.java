package ahodanenok.di.queue;

import ahodanenok.di.character.Character;

/**
 * Sentinel who checks all characters entering the world
 * If character is not welcome, it can reject it by returning false from allow
 */
@FunctionalInterface
public interface Sentinel {

    boolean allow(Character<?> character);
}
