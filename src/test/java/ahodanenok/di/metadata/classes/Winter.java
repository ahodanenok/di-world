package ahodanenok.di.metadata.classes;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@InterceptorBinding
@Ice(type = "light")
@Snow
@Retention(RetentionPolicy.RUNTIME)
public @interface Winter { }
