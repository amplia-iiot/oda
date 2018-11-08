package es.amplia.oda.connector.mqtt;

import es.amplia.oda.connector.mqtt.configuration.ConfigurationUpdateHandlerImpl;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.DispatcherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;

import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQTT external communications bundle activator.
 */
public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private MqttConnector connector;

    private DispatcherProxy dispatcher;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<DeviceInfoProvider> deviceInfoServiceListener;

    private ServiceRegistration<OpenGateConnector> openGateConnectorRegistration;

    private DeviceInfoProviderProxy deviceIdProvider;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("MQTT connector is starting");

        dispatcher = new DispatcherProxy(bundleContext);
        connector = new MqttConnector(dispatcher);
        openGateConnectorRegistration = bundleContext.registerService(OpenGateConnector.class, connector, null);

        deviceIdProvider = new DeviceInfoProviderProxy(bundleContext);
        ConfigurationUpdateHandlerImpl configUpdateHandler =
                new ConfigurationUpdateHandlerImpl(connector, deviceIdProvider);
        configurableBundle = new ConfigurableBundle(bundleContext, configUpdateHandler);
        deviceInfoServiceListener = new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class,
                () -> onServiceChanged(configUpdateHandler));

        LOGGER.info("MQTT connector started");
    }

    private void onServiceChanged(ConfigurationUpdateHandlerImpl configHandler) {
        LOGGER.info("Device Info provider service changed. Reapplying last MQTT connector configuration");
        try {
            configHandler.reapplyConfiguration();
        } catch (Exception e) {
            LOGGER.warn("Error applying configuration");
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("MQTT connector is stopping");

        openGateConnectorRegistration.unregister();
        deviceInfoServiceListener.close();
        configurableBundle.close();
        deviceIdProvider.close();
        connector.close();
        dispatcher.close();

        LOGGER.info("MQTT connector stopped");
    }
}
