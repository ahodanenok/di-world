package ahodanenok.di.next.inject.classes;

import javax.inject.Inject;

public class Supper {

    public Bread bread;

    @Inject
    private Supper(Bread bread) {
        this.bread = bread;
    }
}
