package ahodanenok.di;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EntranceQueue {

    private final Consumer<List<ClassCharacter<?>>> gate;
    private List<ClassCharacter<?>> configs;

    public EntranceQueue(Consumer<List<ClassCharacter<?>>> gate) {
        this.gate = gate;
    }

    public void add(ClassCharacter<?> config) {
        if (configs == null) {
            configs = new ArrayList<>();
        }

        configs.add(config);
    }

    public void flush() {
        List<ClassCharacter<?>> inQueue = configs;
        configs = null;
        gate.accept(inQueue);
    }
}
