package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.countermanager.CounterManager;
import org.osgi.framework.BundleContext;

public class CounterManagerProxy implements CounterManager, AutoCloseable {

    private final OsgiServiceProxy<CounterManager> proxy;

    public CounterManagerProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(CounterManager.class, bundleContext);
    }

    @Override
    public void close() {
        proxy.close();
    }

    @Override
    public void incrementCounter(String key, int amount, int inputs) {
        proxy.consumeFirst(counter -> counter.incrementCounter(key, amount, inputs));
    }

    @Override
    public void incrementCounter(String key, int amount) {
        proxy.consumeFirst(counter -> counter.incrementCounter(key, amount));
    }

    @Override
    public void incrementCounter(String key) {
        proxy.consumeFirst(counter -> counter.incrementCounter(key));
    }
}
