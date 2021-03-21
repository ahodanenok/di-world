package ahodanenok.di;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EntranceQueue {

    private final Consumer<List<ContainerConfiguration>> gate;
    private List<ContainerConfiguration> configs;

    public EntranceQueue(Consumer<List<ContainerConfiguration>> gate) {
        this.gate = gate;
    }

    public void add(ContainerConfiguration config) {
        if (configs == null) {
            configs = new ArrayList<>();
        }

        configs.add(config);
    }

    public void flush() {
        List<ContainerConfiguration> inQueue = configs;
        configs = null;
        gate.accept(inQueue);
    }
}
