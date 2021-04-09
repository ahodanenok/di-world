package ahodanenok.di.next.inject.classes;

import javax.inject.Inject;

public class Cup {

    public Drinkable drinkable;

    @Inject
    public Cup(@Hot Drinkable drinkable) {
        this.drinkable = drinkable;
    }
}
