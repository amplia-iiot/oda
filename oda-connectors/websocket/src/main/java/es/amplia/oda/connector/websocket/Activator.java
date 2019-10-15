package es.amplia.oda.connector.websocket;

import es.amplia.oda.connector.websocket.configuration.WebSocketConfigurationUpdateHandler;
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
    private WebSocketConnector webSocketConnector;
    private WebSocketConfigurationUpdateHandler webSocketConfigHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceRegistration<OpenGateConnector> webSocketConnectorRegistration;
    private ServiceListenerBundle<DeviceInfoProvider> deviceInfoProviderListener;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting WebSocket connector bundle");

        deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        dispatcher = new DispatcherProxy(bundleContext);
        WebSocketClientFactory clientFactory = new WebSocketClientFactory(dispatcher);
        webSocketConnector = new WebSocketConnector(deviceInfoProvider, clientFactory);
        webSocketConfigHandler = new WebSocketConfigurationUpdateHandler(webSocketConnector);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, webSocketConfigHandler);
        webSocketConnectorRegistration = bundleContext.registerService(OpenGateConnector.class, webSocketConnector, null);
        deviceInfoProviderListener =
                new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class, this::onServiceChanged);

        LOGGER.info("WebSocket connector bundle started");
    }

    void onServiceChanged() {
        try {
            webSocketConfigHandler.applyConfiguration();
        } catch (Exception e) {
            LOGGER.error("Error reapplying WebSocket configuration", e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping WebSocket connector bundle");

        webSocketConnectorRegistration.unregister();
        deviceInfoProviderListener.close();
        configurableBundle.close();
        webSocketConnector.close();
        dispatcher.close();
        deviceInfoProvider.close();

        LOGGER.info("WebSocket connector bundle stopped");
    }
}
