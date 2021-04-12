package ahodanenok.di.inject.classes;

import javax.inject.Inject;

public class Supper {

    public Bread bread;

    @Inject
    private Supper(Bread bread) {
        this.bread = bread;
    }
}
