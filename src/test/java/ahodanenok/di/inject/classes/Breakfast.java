package ahodanenok.di.inject.classes;

import javax.inject.Inject;

public class Breakfast {

    public Bread bread;

    @Inject
    public Breakfast(Bread bread) {
        this.bread = bread;
    }
}
