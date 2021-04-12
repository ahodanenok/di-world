package ahodanenok.di.metadata.classes;

import javax.inject.Named;
import javax.interceptor.Interceptors;

@Seasonal(name = "summer")
@Named("I'm an Oak!")
@Interceptors(Forest.class)
public class Oak extends Tree { }
