package ahodanenok.di.inject.classes;

import javax.inject.Inject;

public class P1 extends P0 {

    @Inject
    public SimpleDependency d1;

    @Inject
    public void m1(SimpleDependency d) {
        if (d0 != null) log.add("D0");
        log.add("M1");
    }
}
