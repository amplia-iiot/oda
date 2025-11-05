package es.amplia.oda.comms.http;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import es.amplia.oda.core.commons.http.HttpClientFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Activator implements BundleActivator {

    private ServiceRegistration<HttpClientFactory> httpClientFactoryServiceRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        log.info("Starting HTTP Comms bundle");
        httpClientFactoryServiceRegistration =
                bundleContext.registerService(HttpClientFactory.class, new HttpClientFactoryImpl(), null);
        log.info("HTTP Comms bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        log.info("Stopping HTTP Comms bundle");
        httpClientFactoryServiceRegistration.unregister();
        log.info("HTTP Comms bundle stopped");
    }
}
