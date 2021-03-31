package ahodanenok.di.inject.classes;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class P0 {

    public List<String> log = new ArrayList<>();

    @Inject
    public SimpleDependency d0;
}
