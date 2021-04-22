package ahodanenok.di;

import ahodanenok.di.augment.Augmentation;
import ahodanenok.di.inject.InjectionPoint;
import ahodanenok.di.interceptor.InterceptorChain;
import ahodanenok.di.interceptor.InterceptorRequest;

public interface WorldInternals extends World {

    void pushInjectionPoint(InjectionPoint injectionPoint);

    void popInjectionPoint();

    InterceptorChain getInterceptorChain(InterceptorRequest request);

    Augmentation requestAugmentation();
}
