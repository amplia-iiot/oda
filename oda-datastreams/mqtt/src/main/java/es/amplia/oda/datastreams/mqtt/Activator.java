package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.event.api.EventDispatcherProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private MqttClientFactoryProxy mqttClientFactory;
    private SerializerProxy serializer;
    private EventDispatcherProxy eventDispatcher;
    private MqttDatastreamsConfigurationUpdateHandler configHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<MqttClientFactory> mqttClientFactoryListener;

    private MqttDatastreamsOrchestrator mqttDatastreamsOrchestrator;

    @Override
    public void start(BundleContext bundleContext) {
        mqttClientFactory = new MqttClientFactoryProxy(bundleContext);
        serializer = new SerializerProxy(bundleContext, Serializers.SerializerType.CBOR);
        eventDispatcher = new EventDispatcherProxy(bundleContext);
        ServiceRegistrationManagerWithKey<String, DatastreamsGetter> mqttDatastreamsGetterRegistrationManager =
                new ServiceRegistrationManagerWithKeyOsgi<>(bundleContext, DatastreamsGetter.class);
        ServiceRegistrationManagerWithKey<String, DatastreamsSetter> mqttDatastreamsSetterRegistrationManager =
                new ServiceRegistrationManagerWithKeyOsgi<>(bundleContext, DatastreamsSetter.class);
        mqttDatastreamsOrchestrator = new MqttDatastreamsOrchestrator(mqttClientFactory, serializer, eventDispatcher,
                mqttDatastreamsGetterRegistrationManager, mqttDatastreamsSetterRegistrationManager);
        configHandler = new MqttDatastreamsConfigurationUpdateHandler(mqttDatastreamsOrchestrator);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        mqttClientFactoryListener = new ServiceListenerBundle<>(bundleContext, MqttClientFactory.class,
                this::onServiceChanged);
    }

    void onServiceChanged() {
        try {
            configHandler.applyConfiguration();
        } catch (Exception e) {
            LOGGER.error("Error applying MQTT Datastreams configuration after MQTT Client Factory service changed: {0}",
                    e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        mqttClientFactoryListener.close();
        configurableBundle.close();
        mqttDatastreamsOrchestrator.close();
        mqttClientFactory.close();
        serializer.close();
        eventDispatcher.close();
    }
}
