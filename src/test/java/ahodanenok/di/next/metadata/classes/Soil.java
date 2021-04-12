package ahodanenok.di.next.metadata.classes;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.Interceptor;

@Interceptor
public class Soil {

    @PostConstruct
    public void somethingPlanted() { }

    @PostConstruct
    public void somethingPlantedAgain() { }
}
