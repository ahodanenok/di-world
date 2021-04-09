package ahodanenok.di.next.inject.classes;

import javax.inject.Inject;

public class Morning {

    @Inject
    @Hot
    @Caffeine
    public Drinkable injectedDrink;

    public Drinkable drink;

    @Inject
    public void provideDrink(@Hot @Caffeine Drinkable drink) {
        this.drink = drink;
    }
}
