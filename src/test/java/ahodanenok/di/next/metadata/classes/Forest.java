package ahodanenok.di.next.metadata.classes;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;

@Interceptor
public class Forest {

    @AroundConstruct
    public void onTreeCreated() {

    }

    @AroundInvoke
    protected void onTreeGrowing() {

    }

    @PostConstruct
    void afterTreeCreated() {

    }

    @PreDestroy
    private void beforeTreeDestroyed() {

    }
}
