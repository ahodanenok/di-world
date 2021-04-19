package ahodanenok.di;

import ahodanenok.di.character.Character;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EntranceQueue {

    private final Consumer<List<Character<?>>> gate;
    private List<Character<?>> configs;

    public EntranceQueue(Consumer<List<Character<?>>> gate) {
        this.gate = gate;
    }

    public void add(Character<?> config) {
        if (configs == null) {
            configs = new ArrayList<>();
        }

        configs.add(config);
    }

    public void flush() {
        List<Character<?>> inQueue = configs;
        configs = null;
        gate.accept(inQueue);
    }
}
