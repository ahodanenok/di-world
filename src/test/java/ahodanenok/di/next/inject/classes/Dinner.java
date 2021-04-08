package ahodanenok.di.next.inject.classes;

import javax.inject.Inject;

public class Dinner {

    public Bread bread;

    @Inject
    Dinner(Bread bread) {
        this.bread = bread;
    }
}
