package es.amplia.oda.operation.localprotocoldiscovery;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.operation.api.OperationDiscover;

import es.amplia.oda.operation.localprotocoldiscovery.configuration.LocalProtocolDiscoveryConfigurationUpdateHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<?> registration;
    private MqttClientFactoryProxy mqttClientFactory;
    private SerializerProxy serializer;
    private LocalProtocolDiscoveryConfigurationUpdateHandler configHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<MqttClientFactory> mqttClientFactoryListener;

    @Override
    public void start(BundleContext context) {
        logger.info("Starting Operation Set Activator");
        mqttClientFactory = new MqttClientFactoryProxy(context);
        serializer = new SerializerProxy(context, Serializers.SerializerType.CBOR);
        OperationLocalProtocolDiscoveryImpl operationLocalProtocolDiscovery = new OperationLocalProtocolDiscoveryImpl(mqttClientFactory,
                serializer);
        configHandler = new LocalProtocolDiscoveryConfigurationUpdateHandler(operationLocalProtocolDiscovery);
        configurableBundle = new ConfigurableBundleImpl(context, configHandler);
        registration = context.registerService(OperationDiscover.class.getName(), operationLocalProtocolDiscovery, null);
        mqttClientFactoryListener = new ServiceListenerBundle<>(context, MqttClientFactory.class, this::onServiceChanged);
        logger.info("Operation Set Activator started");
    }

    void onServiceChanged() {
        try {
            configHandler.applyConfiguration();
        } catch (Exception e) {
            logger.error("Error applying Local Protocol Discovery configuration after MQTT Client Factory service changed: {0}",
                    e);
        }
    }

    @Override
    public void stop(BundleContext context) {
        logger.info("Stopping Operation Set Activator");
        mqttClientFactoryListener.close();
        registration.unregister();
        mqttClientFactory.close();
        configurableBundle.close();
        serializer.close();
        logger.info("Operation Set Activator stopped");
    }
}
