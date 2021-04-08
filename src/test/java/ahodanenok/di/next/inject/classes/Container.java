package ahodanenok.di.next.inject.classes;

import org.assertj.core.api.Assertions;

import javax.inject.Inject;

public class Container {

    @Inject
    public Color color;

    public Capacity capacity;

    @Inject
    public void setCapacity(Capacity capacity) {
        Assertions.assertThat(color).isNotNull();
        this.capacity = capacity;
    }
}
