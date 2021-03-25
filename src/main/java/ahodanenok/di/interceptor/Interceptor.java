package ahodanenok.di.interceptor;

import javax.interceptor.InvocationContext;

public interface Interceptor {

    Object execute(InvocationContext context) throws Exception;
}
