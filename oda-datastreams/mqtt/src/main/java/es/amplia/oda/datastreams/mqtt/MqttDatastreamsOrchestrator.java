package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerWithKey;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class MqttDatastreamsOrchestrator implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsOrchestrator.class);

    private final MqttClientFactory mqttClientFactory;
    private final Serializer serializer;
    private final EventDispatcher eventDispatcher;
    private final ServiceRegistrationManagerWithKey<String, DatastreamsGetter> datastreamsGetterRegistrationManager;
    private final ServiceRegistrationManagerWithKey<String, DatastreamsSetter> datastreamsSetterRegistrationManager;

    private MqttClient mqttClient;
    private MqttDatastreamsManager mqttDatastreamsManager;
    private MqttDatastreamsEventHandler mqttDatastreamsEventHandler;
    private MqttDatastreamDiscoveryHandler mqttDatastreamDiscoveryHandler;
    private MqttDatastreamsLwtHandler mqttDatastreamsLwtHandler;

    MqttDatastreamsOrchestrator(MqttClientFactory mqttClientFactory, Serializer serializer,
                                EventDispatcher eventDispatcher,
                                ServiceRegistrationManagerWithKey<String, DatastreamsGetter> datastreamsGetterRegistrationManager,
                                ServiceRegistrationManagerWithKey<String, DatastreamsSetter> datastreamsSetterRegistrationManager) {
        this.mqttClientFactory = mqttClientFactory;
        this.serializer = serializer;
        this.eventDispatcher = eventDispatcher;
        this.datastreamsGetterRegistrationManager = datastreamsGetterRegistrationManager;
        this.datastreamsSetterRegistrationManager = datastreamsSetterRegistrationManager;
    }

    void loadConfiguration(MqttDatastreamsConfiguration configuration,
                           List<DatastreamInfoWithPermission> initialDatastreamsConfiguration) throws MqttException {
        closeResources();
        mqttClient = mqttClientFactory.createMqttClient(configuration.getServerURI(), configuration.getClientId());
        mqttClient.connect();
        MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager = new MqttDatastreamsPermissionManager();
        MqttDatastreamsFactory mqttDatastreamsFactory =
                new MqttDatastreamsFactory(mqttClient, mqttDatastreamsPermissionManager, serializer, eventDispatcher,
                        configuration.getReadRequestTopic(), configuration.getReadResponseTopic(),
                        configuration.getWriteRequestTopic(), configuration.getWriteResponseTopic(),
                        configuration.getEventTopic());
        mqttDatastreamsEventHandler = mqttDatastreamsFactory.createDatastreamsEventHandler();
        mqttDatastreamsManager = new MqttDatastreamsManager(datastreamsGetterRegistrationManager,
                datastreamsSetterRegistrationManager, mqttDatastreamsFactory);
        mqttDatastreamDiscoveryHandler = new MqttDatastreamDiscoveryHandler(mqttClient, serializer,
                mqttDatastreamsManager, mqttDatastreamsPermissionManager, configuration.getEnableDatastreamTopic(),
                configuration.getDisableDatastreamTopic());
        mqttDatastreamDiscoveryHandler.init(initialDatastreamsConfiguration);
        mqttDatastreamsLwtHandler = new MqttDatastreamsLwtHandler(mqttClient, serializer,
                mqttDatastreamsPermissionManager, mqttDatastreamsManager, configuration.getLwtTopic());
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
            if (mqttDatastreamsEventHandler != null) {
                mqttDatastreamsEventHandler.close();
                mqttDatastreamsEventHandler = null;
            }
            if (mqttDatastreamsManager!= null) {
                mqttDatastreamsManager.close();
                mqttDatastreamsManager = null;
            }
            if (mqttClient != null) {
                mqttClient.disconnect();
                mqttClient = null;
            }
        } catch (MqttException e) {
            LOGGER.warn("Error closing MQTT resources {0}", e);
        }
    }

    @Override
    public void close() {
        closeResources();
    }
}
