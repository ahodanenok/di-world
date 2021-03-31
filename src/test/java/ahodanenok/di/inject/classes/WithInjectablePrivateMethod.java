package ahodanenok.di.inject.classes;

import javax.inject.Inject;

public class WithInjectablePrivateMethod {

    public SimpleDependency d;
    public boolean called_1;
    public boolean called_2;

    @Inject
    private void method_1(SimpleDependency d) {
        this.d = d;
        this.called_1 = true;
    }

    private void method_2() {
        this.called_2 = true;
    }
}
