package es.amplia.oda.connector.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.connector.mqtt.configuration.ConfigurationUpdateHandlerImpl;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.DispatcherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;

import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private MqttConnector connector;

    private MqttClientFactoryProxy mqttClientFactory;
    private DispatcherProxy dispatcher;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<MqttClientFactory> mqttClientFactoryServiceListener;
    private ServiceListenerBundle<DeviceInfoProvider> deviceInfoServiceListener;

    private ServiceRegistration<OpenGateConnector> openGateConnectorRegistration;

    private DeviceInfoProviderProxy deviceIdProvider;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting MQTT connector");

        mqttClientFactory = new MqttClientFactoryProxy(bundleContext);
        dispatcher = new DispatcherProxy(bundleContext);
        connector = new MqttConnector(mqttClientFactory, dispatcher);
        openGateConnectorRegistration = bundleContext.registerService(OpenGateConnector.class, connector, null);

        deviceIdProvider = new DeviceInfoProviderProxy(bundleContext);
        ConfigurationUpdateHandlerImpl configUpdateHandler =
                new ConfigurationUpdateHandlerImpl(connector, deviceIdProvider);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configUpdateHandler);
        mqttClientFactoryServiceListener = new ServiceListenerBundle<>(bundleContext, MqttClientFactory.class,
                () -> onServiceChanged(configUpdateHandler));
        deviceInfoServiceListener = new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class,
                () -> onServiceChanged(configUpdateHandler));

        LOGGER.info("MQTT connector started");
    }

    void onServiceChanged(ConfigurationUpdateHandlerImpl configHandler) {
        LOGGER.info("Device Info provider service changed. Reapplying last MQTT connector configuration");
        try {
            configHandler.reapplyConfiguration();
            LOGGER.info("Last MQTT connector configuration reapplied");
        } catch (Exception e) {
            LOGGER.error("Error applying configuration: ", e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping MQTT connector");

        openGateConnectorRegistration.unregister();
        mqttClientFactoryServiceListener.close();
        deviceInfoServiceListener.close();
        configurableBundle.close();
        deviceIdProvider.close();
        connector.close();
        mqttClientFactory.close();
        dispatcher.close();

        LOGGER.info("MQTT connector stopped");
    }
}
