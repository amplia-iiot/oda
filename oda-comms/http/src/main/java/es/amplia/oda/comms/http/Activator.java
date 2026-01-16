package es.amplia.oda.comms.http;

import es.amplia.oda.comms.http.configuration.HttpCommsConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import es.amplia.oda.core.commons.http.HttpClientFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Activator implements BundleActivator {

    private ServiceRegistration<HttpClientFactory> httpClientFactoryServiceRegistration;
    private ConfigurableBundle configurableBundle;

    @Override
    public void start(BundleContext bundleContext) {
        log.info("Starting HTTP Comms bundle");
        HttpClientFactoryImpl httpClientFactory = new HttpClientFactoryImpl();

        httpClientFactoryServiceRegistration =
                bundleContext.registerService(HttpClientFactory.class, httpClientFactory, null);

        // make bundle configurable
        HttpCommsConfigurationUpdateHandler configUpdateHandler = new HttpCommsConfigurationUpdateHandler(httpClientFactory);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configUpdateHandler);

        log.info("HTTP Comms bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        log.info("Stopping HTTP Comms bundle");
        httpClientFactoryServiceRegistration.unregister();
        configurableBundle.close();
        log.info("HTTP Comms bundle stopped");
    }
}
