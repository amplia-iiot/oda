package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.Interceptor;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Arrays;
import java.util.Optional;

public class InterceptorProxy<T, S extends Interceptor<T>> implements Interceptor<T>, AutoCloseable {

    /**
     * Class type of the interceptor.
     */
    private final Class<S> interceptorClass;

    private final ServiceTracker<S, S> interceptorServiceTracker;

    public InterceptorProxy(Class<S> interceptorClass, BundleContext bundleContext) {
        this.interceptorClass = interceptorClass;
        this.interceptorServiceTracker = new ServiceTracker<>(bundleContext, interceptorClass, null);
        interceptorServiceTracker.open();
    }


    @Override
    public Optional<T> intercept(Optional<T> param) {
        Object[] eventInterceptors = interceptorServiceTracker.getServices();
        if (eventInterceptors == null) {
            return param;
        }

        return Arrays.stream(eventInterceptors)
                .map(interceptorClass::cast)
                .reduce(param,
                        (currentParam, interceptor) -> interceptor.intercept(currentParam),
                        (param1, param2) -> param2);
    }

    @Override
    public void close() {
        interceptorServiceTracker.close();
    }
}
