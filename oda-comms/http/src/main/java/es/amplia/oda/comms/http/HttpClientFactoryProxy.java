package es.amplia.oda.comms.http;

import org.osgi.framework.BundleContext;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;

public class HttpClientFactoryProxy implements HttpClientFactory, AutoCloseable {

    private final OsgiServiceProxy<HttpClientFactory> proxy;

    public HttpClientFactoryProxy(BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(HttpClientFactory.class, bundleContext);
    }

    @Override
    public HttpClient createClient() {
        return proxy.callFirst(factory -> {
                return factory.createClient();
            });
    }

    @Override
    public HttpClient createClient(boolean insecure) {
        return proxy.callFirst(factory -> {
                return factory.createClient(insecure);
            });
    }

    @Override
    public void close() {
        proxy.close();
    }
}
