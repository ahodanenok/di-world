package ahodanenok.di;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ContainerConfiguration {

    public static ContainerConfiguration ofClass(Class<?> clazz) {
        // todo: read configuration from annotations
        return new ContainerConfiguration(clazz);
    }

    private Class<?> type;
    private Set<String> names = new HashSet<>();

    public ContainerConfiguration(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public ContainerConfiguration withNames(String... names) {
        this.names.addAll(Arrays.asList(names));
        return this;
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(names);
    }
}
