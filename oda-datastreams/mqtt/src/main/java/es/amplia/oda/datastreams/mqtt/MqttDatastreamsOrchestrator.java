package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttConnectOptions.MqttConnectOptionsBuilder;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.OperationSender;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.datastreams.mqtt.configuration.MqttDatastreamsConfiguration;
import es.amplia.oda.event.api.ResponseDispatcher;

import java.util.HashSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttDatastreamsOrchestrator implements MqttDatastreamsService, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsOrchestrator.class);

    private final MqttClientFactory mqttClientFactory;
    private final Serializer serializer;
    private final EventPublisher eventPublisher;
    private final BundleContext bundleContext;
    private final DeviceInfoProviderProxy deviceInfoProvider;
    private final ResponseDispatcher responseDispatcher;

    private boolean ready;
    private MqttClient mqttClient;
    private MqttDatastreamsEvent mqttDatastreamsEvent;

    private ServiceRegistration<OperationSender> mqttOperationSenderRegistration;

    MqttDatastreamsOrchestrator(MqttClientFactory mqttClientFactory, Serializer serializer, EventPublisher eventPublisher,
                                DeviceInfoProviderProxy deviceInfoProvider, ResponseDispatcher respDispatcher, BundleContext bundleContext) {
        this.mqttClientFactory = mqttClientFactory;
        this.serializer = serializer;
        this.eventPublisher = eventPublisher;
        this.deviceInfoProvider = deviceInfoProvider;
        this.responseDispatcher = respDispatcher;
        this.bundleContext = bundleContext;
        this.ready = false;
    }

    public void loadConfiguration(MqttDatastreamsConfiguration configuration) {
        closeResources();
        mqttClient = mqttClientFactory.createMqttClient(configuration.getServerURI(), configuration.getClientId());
        if (mqttClient != null) {
            // Quiere decir que ya tenemos accesible el bundle MQTTClientFactory
            MqttConnectOptionsBuilder options = MqttConnectOptions.builder(configuration.getClientId(), configuration.getPassword().toCharArray());
            if (configuration.getKeyStore() != null) options.ssl(configuration.getKeyStore(), configuration.getKeyStorePassword(), configuration.getTrustStore(), configuration.getTrustStorePassword());
            HashSet<String> odaList = new HashSet<>();
            if (configuration.getNextLevelOdaIds() != null) odaList.addAll(configuration.getNextLevelOdaIds());
            mqttDatastreamsEvent = new MqttDatastreamsEvent(eventPublisher, mqttClient, serializer, configuration.getEventTopic(), deviceInfoProvider, configuration.getResponseTopic(), responseDispatcher, odaList);
            MqttOperationSender mqttOperationSender = new MqttOperationSender(mqttClient, serializer, configuration.getRequestTopic(), configuration.getQos(), configuration.isRetained(), odaList);
            this.mqttOperationSenderRegistration = this.bundleContext.registerService(OperationSender.class, mqttOperationSender, null);
            this.ready = true;
            mqttClient.connect(options.build()); // Lo dejamos al final porque puede tardar en conectar y si falla estará el reconnect automático del MQTT
        }
    }

    private void closeResources() {
        try {
            if (mqttDatastreamsEvent != null) {
                mqttDatastreamsEvent.unregisterFromEventSource();
                mqttDatastreamsEvent = null;
            }
            if (mqttOperationSenderRegistration != null) {
                mqttOperationSenderRegistration.unregister();
                mqttOperationSenderRegistration = null;
            }
            if (mqttClient != null) {
                mqttClient.disconnect();
                mqttClient = null;
            }
            this.ready = false;
        } catch (MqttException e) {
            LOGGER.warn("Error closing MQTT resources {0}", e);
        }
    }

    @Override
    public void close() {
        closeResources();
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
