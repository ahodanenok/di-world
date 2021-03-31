package ahodanenok.di.inject.classes;

import javax.inject.Inject;

public class P2 extends P1 {

    @Inject public SimpleDependency d2;

    @Inject
    public void m2(SimpleDependency d) {
        if (d1 != null) log.add("D1");
        log.add("M2");
    }
}
