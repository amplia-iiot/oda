package es.amplia.oda.connector.http;

import es.amplia.oda.connector.http.configuration.ConnectorConfigurationUpdateHandler;
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
    private ConnectorConfigurationUpdateHandler configHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceRegistration<OpenGateConnector> openGateConnectorRegistration;
    private ServiceListenerBundle<DeviceInfoProvider> deviceInfoProviderListener;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting HTTP connector bundle");

        deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        HttpConnector connector = new HttpConnector(deviceInfoProvider);
        configHandler = new ConnectorConfigurationUpdateHandler(connector);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        openGateConnectorRegistration = bundleContext.registerService(OpenGateConnector.class, connector, null);
        deviceInfoProviderListener =
                new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class, this::onServiceChanged);

        LOGGER.info("HTTP connector bundle started");
    }

    void onServiceChanged() {
        LOGGER.info("Device Info Provider service changed. Reapplying last configuration");
        try {
            configHandler.applyConfiguration();
        } catch (Exception e) {
            LOGGER.info("Error reapplying last configuration: {}", e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopped HTTP connector bundle");

        openGateConnectorRegistration.unregister();
        deviceInfoProviderListener.close();
        configurableBundle.close();
        deviceInfoProvider.close();

        LOGGER.info("HTTP connector bundle stopped");
    }
}
