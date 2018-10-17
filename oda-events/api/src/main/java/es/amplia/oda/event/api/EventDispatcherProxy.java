package es.amplia.oda.event.api;

import es.amplia.oda.core.commons.osgi.proxies.InterceptorProxy;
import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;

import org.osgi.framework.BundleContext;

import java.util.Optional;

public class EventDispatcherProxy implements EventDispatcher, AutoCloseable {

    private final OsgiServiceProxy<EventDispatcher> proxy;

    private final InterceptorProxy<Event, EventInterceptor> eventInterceptorProxy;

    public EventDispatcherProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(EventDispatcher.class, bundleContext);
        eventInterceptorProxy = new InterceptorProxy<>(EventInterceptor.class, bundleContext);
    }

    @Override
    public void publish(Event event) {
        Optional<Event> resultEvent = eventInterceptorProxy.intercept(Optional.of(event));
        resultEvent.ifPresent(value -> proxy.consumeAll(eventDispatcher -> eventDispatcher.publish(value)));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
