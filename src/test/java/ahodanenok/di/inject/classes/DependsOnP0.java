package ahodanenok.di.inject.classes;

import javax.inject.Inject;

public class DependsOnP0 {

    public P0 p;

    @Inject
    public void setP(P0 p) {
        this.p = p;
    }
}
