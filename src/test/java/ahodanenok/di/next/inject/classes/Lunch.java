package ahodanenok.di.next.inject.classes;

import javax.inject.Inject;

public class Lunch {

    public Bread bread;

    @Inject
    protected Lunch(Bread bread) {
        this.bread = bread;
    }
}
