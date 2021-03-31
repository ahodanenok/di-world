package ahodanenok.di.inject.classes;

import javax.inject.Inject;

public class P3 extends P2 {

    @Inject public SimpleDependency d3;

    @Inject
    public void m3(SimpleDependency d) {
        if (d2 != null) log.add("D2");
        log.add("M3");
    }
}
