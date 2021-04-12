package ahodanenok.di.character.classes;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
public class ConsumptionTracker {

    @PreDestroy
    private void preDestroy(InvocationContext context) throws Exception { }

    @PostConstruct
    void postConstruct() { }

    @AroundInvoke
    protected void aroundInvoke(InvocationContext context) { }

    @AroundConstruct
    public void aroundConstruct() throws Exception { }
}
