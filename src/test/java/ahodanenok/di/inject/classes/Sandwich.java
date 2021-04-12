package ahodanenok.di.inject.classes;

public class Sandwich {

    public Bread bread;
    public Butter butter;

    public Sandwich(Bread bread, Butter butter) {
        this.bread = bread;
        this.butter = butter;
    }
}
