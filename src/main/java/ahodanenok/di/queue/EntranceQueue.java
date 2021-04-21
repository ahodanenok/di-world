package ahodanenok.di.queue;

import ahodanenok.di.character.Character;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EntranceQueue {

    private final Consumer<List<Character<?>>> gate;
    private List<Character<?>> configs;
    private Sentinel sentinel = __ -> true;

    public EntranceQueue(Consumer<List<Character<?>>> gate) {
        this.gate = gate;
    }

    /**
     * Assign a new sentinel who will check all characters trying to reach the world
     */
    public void assignSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
    }

    public void add(Character<?> config) {
        if (configs == null) {
            configs = new ArrayList<>();
        }

        configs.add(config);
    }

    public void flush() {
        // remembering list at the flush time
        List<Character<?>> inQueue = configs;
        configs = null;

        // sentinel will check incoming characters
        gate.accept(inQueue.stream().filter(ch -> sentinel.allow(ch)).collect(Collectors.toList()));
    }
}
