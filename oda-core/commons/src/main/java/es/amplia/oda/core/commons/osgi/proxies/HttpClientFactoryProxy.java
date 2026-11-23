package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.http.HttpClient;
import es.amplia.oda.core.commons.http.HttpClientFactory;

import org.osgi.framework.BundleContext;

public class HttpClientFactoryProxy implements HttpClientFactory, AutoCloseable {

    private final OsgiServiceProxy<HttpClientFactory> proxy;

    public HttpClientFactoryProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(HttpClientFactory.class, bundleContext);
    }

    @Override
    public HttpClient createHttpClient() {
        return proxy.callFirst(HttpClientFactory::createHttpClient);
    }

    @Override
    public HttpClient createHttpClient(boolean insecure) {
        return proxy.callFirst(factory -> factory.createHttpClient(insecure));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
