package ahodanenok.di.next.metadata.classes;

import javax.inject.Qualifier;
import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface Evergreen { }
