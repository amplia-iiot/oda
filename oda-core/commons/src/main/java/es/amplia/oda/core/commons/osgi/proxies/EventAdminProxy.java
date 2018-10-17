package es.amplia.oda.core.commons.osgi.proxies;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class EventAdminProxy implements EventAdmin, AutoCloseable {

    private final OsgiServiceProxy<EventAdmin> proxy;

	public EventAdminProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(EventAdmin.class, bundleContext);
	}

    @Override
    public void postEvent(Event event) {
	    proxy.consumeFirst(eventAdmin -> eventAdmin.postEvent(event));
    }

    @Override
    public void sendEvent(Event event) {
        proxy.consumeFirst(eventAdmin -> eventAdmin.sendEvent(event));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
