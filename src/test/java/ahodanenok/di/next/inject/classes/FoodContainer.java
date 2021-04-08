package ahodanenok.di.next.inject.classes;

import org.assertj.core.api.Assertions;

import javax.inject.Inject;

public class FoodContainer extends Container {

    public Bread bread;

    @Inject
    public Butter butter;

    @Inject
    public void setBread(Bread bread) {
        Assertions.assertThat(butter).isNotNull();
        Assertions.assertThat(capacity).isNotNull();
        Assertions.assertThat(color).isNotNull();

        this.bread = bread;
    }
}
