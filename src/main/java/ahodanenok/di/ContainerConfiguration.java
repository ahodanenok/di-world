package ahodanenok.di;

import ahodanenok.di.scope.AlwaysNewScope;
import ahodanenok.di.scope.Scope;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ContainerConfiguration<T> {

    public static <T> ContainerConfiguration<T> ofClass(Class<T> clazz) {
        // todo: read configuration from annotations
        return new ContainerConfiguration<T>(clazz);
    }

    private Class<T> type;
    private Set<String> names = new HashSet<>();
    private Scope<T> scope = AlwaysNewScope.getInstance();

    public ContainerConfiguration(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }

    public ContainerConfiguration<T> withNames(String... names) {
        this.names.addAll(Arrays.asList(names));
        return this;
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(names);
    }

    public ContainerConfiguration<T> withScope(Scope<T> scope) {
        // todo: check not null
        this.scope = scope;
        return this;
    }

    public Scope<T> getScope() {
        return scope;
    }
}
