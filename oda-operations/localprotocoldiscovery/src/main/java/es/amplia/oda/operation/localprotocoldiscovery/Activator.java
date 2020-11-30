package es.amplia.oda.operation.localprotocoldiscovery;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import es.amplia.oda.core.commons.osgi.proxies.MqttDatastreamsServiceProxy;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);


    private MqttClientFactoryProxy mqttClientFactory;
    private MqttDatastreamsServiceProxy mqttDatastreamsService;
    private SerializerProxy serializer;
    private LocalProtocolDiscoveryConfigurationUpdateHandler configHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceRegistration<OperationDiscover> registration;
    private ServiceListenerBundle<MqttClientFactory> mqttClientFactoryListener;
    private ServiceListenerBundle<MqttDatastreamsService> mqttDatastreamsServiceListener;


    @Override
    public void start(BundleContext context) {
        LOGGER.info("Starting Operation Discover");
        mqttClientFactory = new MqttClientFactoryProxy(context);
        mqttDatastreamsService = new MqttDatastreamsServiceProxy(context);
        serializer = new SerializerProxy(context, ContentType.CBOR);
        OperationLocalProtocolDiscoveryImpl operationLocalProtocolDiscovery =
                new OperationLocalProtocolDiscoveryImpl(mqttClientFactory, mqttDatastreamsService, serializer);
        configHandler = new LocalProtocolDiscoveryConfigurationUpdateHandler(operationLocalProtocolDiscovery);
        configurableBundle = new ConfigurableBundleImpl(context, configHandler);
        registration = context.registerService(OperationDiscover.class, operationLocalProtocolDiscovery, null);
        mqttClientFactoryListener = new ServiceListenerBundle<>(context, MqttClientFactory.class, this::onServiceChanged);
        mqttDatastreamsServiceListener = new ServiceListenerBundle<>(context, MqttDatastreamsService.class, this::onServiceChanged);
        LOGGER.info("Operation Discover started");
    }

    void onServiceChanged() {
        try {
            configHandler.applyConfiguration();
        } catch (Exception e) {
            LOGGER.error("Error applying Local Protocol Discovery configuration after MQTT Client Factory service changed: {0}",
                    e);
        }
    }

    @Override
    public void stop(BundleContext context) {
        LOGGER.info("Stopping Operation Discover");
        mqttDatastreamsServiceListener.close();
        mqttClientFactoryListener.close();
        registration.unregister();
        mqttClientFactory.close();
        mqttDatastreamsService.close();
        configurableBundle.close();
        serializer.close();
        LOGGER.info("Operation Discover stopped");
    }
}
