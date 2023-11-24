package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.*;

import es.amplia.oda.datastreams.mqtt.configuration.MqttDatastreamsConfigurationUpdateHandler;
import es.amplia.oda.event.api.ResponseDispatcher;
import es.amplia.oda.event.api.ResponseDispatcherProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private MqttClientFactoryProxy mqttClientFactory;
    private SerializerProxy serializer;
    private EventPublisherProxy eventPublisher;
    private MqttDatastreamsConfigurationUpdateHandler configHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceRegistration<MqttDatastreamsService> mqttDatastreamServiceRegistration;
    private ServiceListenerBundle<MqttClientFactory> mqttClientFactoryListener;
    private MqttDatastreamsOrchestrator mqttDatastreamsOrchestrator;
    private DeviceInfoProviderProxy deviceInfoProvider;
    private ResponseDispatcherProxy responseDispatcher;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting MQTT datastreams bundle");
        mqttClientFactory = new MqttClientFactoryProxy(bundleContext);
        serializer = new SerializerProxy(bundleContext, ContentType.JSON);
        deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        eventPublisher = new EventPublisherProxy(bundleContext);
        responseDispatcher = new ResponseDispatcherProxy(bundleContext);
        mqttDatastreamsOrchestrator = new MqttDatastreamsOrchestrator(mqttClientFactory, serializer, eventPublisher, deviceInfoProvider, responseDispatcher, bundleContext);
        configHandler = new MqttDatastreamsConfigurationUpdateHandler(mqttDatastreamsOrchestrator);
        mqttDatastreamServiceRegistration = bundleContext.registerService(MqttDatastreamsService.class,
                mqttDatastreamsOrchestrator, null);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        mqttClientFactoryListener = new ServiceListenerBundle<>(bundleContext, MqttClientFactory.class,
                this::onServiceChanged);
        LOGGER.info("MQTT datastreams bundle started");
    }

    void onServiceChanged() {
        try {
            LOGGER.info("Applying new configuration for MQTT datastreams bundle");
            configHandler.applyConfiguration();
        } catch (Exception e) {
            LOGGER.error("Error applying MQTT Datastreams configuration after MQTT Client Factory service changed: {0}",
                    e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping MQTT datastreams bundle");
        mqttClientFactoryListener.close();
        configurableBundle.close();
        mqttDatastreamServiceRegistration.unregister();
        mqttDatastreamsOrchestrator.close();
        mqttClientFactory.close();
        serializer.close();
        eventPublisher.close();
        deviceInfoProvider.close();
        responseDispatcher.close();
        LOGGER.info("MQTT datastreams bundle stopped");
    }
}
