package ahodanenok.di.next.metadata.classes;

import javax.inject.Named;
import javax.interceptor.Interceptors;

@Named
@Needles
@Evergreen
@Interceptors({Soil.class, Forest.class, Seasons.class})
public class Spruce extends Tree { }
