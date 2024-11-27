package es.amplia.oda.comms.http;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<HttpClientFactory> httpClientFactoryServiceRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting HTTP Comms bundle");
        httpClientFactoryServiceRegistration =
                bundleContext.registerService(HttpClientFactory.class, new HttpClientFactoryImpl(), null);
        LOGGER.info("HTTP Comms bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping HTTP Comms bundle");
        httpClientFactoryServiceRegistration.unregister();
        LOGGER.info("HTTP Comms bundle stopped");
    }
}
