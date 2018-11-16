package es.amplia.oda.connector.websocket;

import es.amplia.oda.connector.websocket.configuration.ConnectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.DispatcherProxy;
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
    private DispatcherProxy dispatcher;
    private WebSocketConnector connector;
    private ConnectorConfigurationUpdateHandler configHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceRegistration<OpenGateConnector> openGateConnectorRegistration;
    private ServiceListenerBundle<DeviceInfoProvider> deviceInfoProviderListener;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting WebSocket connector bundle");

        deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        dispatcher = new DispatcherProxy(bundleContext);
        WebSocketClientFactory clientFactory = new WebSocketClientFactory(dispatcher);
        connector = new WebSocketConnector(deviceInfoProvider, clientFactory);
        configHandler = new ConnectorConfigurationUpdateHandler(connector);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        openGateConnectorRegistration = bundleContext.registerService(OpenGateConnector.class, connector, null);
        deviceInfoProviderListener =
                new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class, this::onServiceChanged);

        LOGGER.info("WebSocket connector bundle started");
    }

    void onServiceChanged() {
        LOGGER.info("Device Info Provider service changed. Reapplying WebSocket connector configuration");
        try {
            configHandler.applyConfiguration();
        } catch (Exception e) {
            LOGGER.error("Error reapplying configuration: {}", e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping WebSocket connector bundle");

        openGateConnectorRegistration.unregister();
        deviceInfoProviderListener.close();
        configurableBundle.close();
        connector.close();
        dispatcher.close();
        deviceInfoProvider.close();

        LOGGER.info("WebSocket connector bundle stopped");
    }
}
