package es.amplia.oda.event.api;

import es.amplia.oda.core.commons.osgi.proxies.InterceptorProxy;
import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;

import es.amplia.oda.core.commons.utils.Event;
import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.Optional;

public class EventDispatcherProxy implements EventDispatcher, AutoCloseable {

    private final OsgiServiceProxy<EventDispatcher> proxy;

    private final InterceptorProxy<List<Event>, EventListInterceptor> eventListInterceptorProxy;

    public EventDispatcherProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(EventDispatcher.class, bundleContext);
        eventListInterceptorProxy = new InterceptorProxy<>(EventListInterceptor.class, bundleContext);
    }

    @Override
    public void publish(List<Event> event) {
        Optional<List<Event>> resultEvent = eventListInterceptorProxy.intercept(Optional.of(event));
        resultEvent.ifPresent(value -> proxy.consumeAll(eventDispatcher -> eventDispatcher.publish(value)));
    }

    @Override
    public void publishImmediately(List<Event> event) {
        Optional<List<Event>> resultEvent = eventListInterceptorProxy.intercept(Optional.of(event));
        resultEvent.ifPresent(value -> proxy.consumeAll(eventDispatcher -> eventDispatcher.publishImmediately(value)));
    }

    @Override
    public void publishSameThreadNoQos(List<Event> event) {
        Optional<List<Event>> resultEvent = eventListInterceptorProxy.intercept(Optional.of(event));
        resultEvent.ifPresent(value -> proxy.consumeAll(eventDispatcher -> eventDispatcher.publishSameThreadNoQos(value)));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
