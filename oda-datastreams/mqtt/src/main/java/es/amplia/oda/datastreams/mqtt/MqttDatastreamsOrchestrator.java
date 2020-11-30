package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerWithKey;

import es.amplia.oda.datastreams.mqtt.configuration.MqttDatastreamsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MqttDatastreamsOrchestrator implements MqttDatastreamsService, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsOrchestrator.class);

    private final MqttClientFactory mqttClientFactory;
    private final Serializer serializer;
    private final EventPublisher eventPublisher;
    private final ServiceRegistrationManagerWithKey<String, DatastreamsGetter> datastreamsGetterRegistrationManager;
    private final ServiceRegistrationManagerWithKey<String, DatastreamsSetter> datastreamsSetterRegistrationManager;

    private boolean ready;
    private MqttClient mqttClient;
    private MqttDatastreamsManager mqttDatastreamsManager;
    private MqttDatastreamsEvent mqttDatastreamsEvent;
    private MqttDatastreamDiscoveryHandler mqttDatastreamDiscoveryHandler;
    private MqttDatastreamsLwtHandler mqttDatastreamsLwtHandler;

    MqttDatastreamsOrchestrator(MqttClientFactory mqttClientFactory, Serializer serializer, EventPublisher eventPublisher,
                                ServiceRegistrationManagerWithKey<String, DatastreamsGetter> datastreamsGetterRegistrationManager,
                                ServiceRegistrationManagerWithKey<String, DatastreamsSetter> datastreamsSetterRegistrationManager) {
        this.mqttClientFactory = mqttClientFactory;
        this.serializer = serializer;
        this.eventPublisher = eventPublisher;
        this.datastreamsGetterRegistrationManager = datastreamsGetterRegistrationManager;
        this.datastreamsSetterRegistrationManager = datastreamsSetterRegistrationManager;
        this.ready = false;
    }

    public void loadConfiguration(MqttDatastreamsConfiguration configuration,
                                  List<DatastreamInfoWithPermission> initialDatastreamsConfiguration) {
        closeResources();
        mqttClient = mqttClientFactory.createMqttClient(configuration.getServerURI(), configuration.getClientId());
        mqttClient.connect();
        MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager = new MqttDatastreamsPermissionManager();
        MqttDatastreamsFactory mqttDatastreamsFactory =
                new MqttDatastreamsFactory(mqttClient, mqttDatastreamsPermissionManager, serializer, eventPublisher,
                        configuration.getReadRequestTopic(), configuration.getReadResponseTopic(),
                        configuration.getWriteRequestTopic(), configuration.getWriteResponseTopic(),
                        configuration.getEventTopic());
        mqttDatastreamsEvent = mqttDatastreamsFactory.createDatastreamsEvent();
        mqttDatastreamsManager = new MqttDatastreamsManager(datastreamsGetterRegistrationManager,
                datastreamsSetterRegistrationManager, mqttDatastreamsFactory);
        mqttDatastreamDiscoveryHandler = new MqttDatastreamDiscoveryHandler(mqttClient, serializer,
                mqttDatastreamsManager, mqttDatastreamsPermissionManager, configuration.getEnableDatastreamTopic(),
                configuration.getDisableDatastreamTopic());
        mqttDatastreamDiscoveryHandler.init(initialDatastreamsConfiguration);
        mqttDatastreamsLwtHandler = new MqttDatastreamsLwtHandler(mqttClient, serializer,
                mqttDatastreamsPermissionManager, mqttDatastreamsManager, configuration.getLwtTopic());
        this.ready = true;
    }

    private void closeResources() {
        try {
            if (mqttDatastreamsLwtHandler != null) {
                mqttDatastreamsLwtHandler.close();
                mqttDatastreamsLwtHandler = null;
            }
            if (mqttDatastreamDiscoveryHandler != null) {
                mqttDatastreamDiscoveryHandler.close();
                mqttDatastreamDiscoveryHandler = null;
            }
            if (mqttDatastreamsEvent != null) {
                mqttDatastreamsEvent.unregisterFromEventSource();
                mqttDatastreamsEvent = null;
            }
            if (mqttDatastreamsManager!= null) {
                mqttDatastreamsManager.close();
                mqttDatastreamsManager = null;
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
