package es.amplia.oda.connector.http;

import es.amplia.oda.comms.http.HttpClientFactoryImpl;
import es.amplia.oda.connector.http.configuration.HttpConnectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.http.HttpClientFactory;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private DeviceInfoProviderProxy deviceInfoProvider;
    private HttpConnectorConfigurationUpdateHandler httpConfigHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceRegistration<OpenGateConnector> httpConnectorRegistration;
    private ServiceListenerBundle<DeviceInfoProvider> deviceInfoProviderListener;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting HTTP connector bundle");

        deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        HttpClientFactory httpClientFactory = new HttpClientFactoryImpl();
        HttpConnector httpConnector = new HttpConnector(deviceInfoProvider, httpClientFactory);
        httpConfigHandler = new HttpConnectorConfigurationUpdateHandler(httpConnector);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, httpConfigHandler);
        httpConnectorRegistration = bundleContext.registerService(OpenGateConnector.class, httpConnector, null);
        deviceInfoProviderListener =
                new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class, this::onServiceChanged);

        LOGGER.info("HTTP connector bundle started");
    }

    void onServiceChanged() {
        LOGGER.info("Device Info provider service changed. Applying HTTP connector configuration");

        try {
            httpConfigHandler.applyConfiguration();
        } catch (Exception e) {
            LOGGER.info("Error applying HTTP connector configuration", e);
        }

        LOGGER.info("HTTP connector configuration applied");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopped HTTP connector bundle");

        httpConnectorRegistration.unregister();
        deviceInfoProviderListener.close();
        configurableBundle.close();
        deviceInfoProvider.close();

        LOGGER.info("HTTP connector bundle stopped");
    }
}
