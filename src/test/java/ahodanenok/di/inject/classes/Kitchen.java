package ahodanenok.di.inject.classes;

import javax.inject.Inject;
import java.util.List;

public class Kitchen {

    public List<Drinkable> drinks;
    public List<Drinkable> hotDrinks;

    @Inject
    public void drinks(List<Drinkable> drinks) {
        this.drinks = drinks;
    }

    @Inject
    public void hotDrinks(@Hot List<Drinkable> drinks) {
        this.hotDrinks = drinks;
    }
}
