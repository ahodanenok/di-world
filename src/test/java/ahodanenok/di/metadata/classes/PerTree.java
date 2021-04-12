package ahodanenok.di.metadata.classes;

import javax.inject.Scope;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Scope
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface PerTree { }
