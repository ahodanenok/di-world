package ahodanenok.di.metadata.classes;

import javax.annotation.PostConstruct;
import javax.interceptor.Interceptor;

@Interceptor
public class Soil {

    @PostConstruct
    public void somethingPlanted() { }

    @PostConstruct
    public void somethingPlantedAgain() { }
}
