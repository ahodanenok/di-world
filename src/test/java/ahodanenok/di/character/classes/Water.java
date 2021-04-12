package ahodanenok.di.character.classes;

import javax.interceptor.Interceptors;

@Cold
@Tasteless
@Interceptors({Filter.class, ConsumptionTracker.class})
public class Water {

}
