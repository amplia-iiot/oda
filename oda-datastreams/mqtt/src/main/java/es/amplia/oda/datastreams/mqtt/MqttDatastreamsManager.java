package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerWithKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


class MqttDatastreamsManager implements AutoCloseable {

    private final ServiceRegistrationManagerWithKey<String, DatastreamsGetter> datastreamsGetterRegistrationManager;
    private final ServiceRegistrationManagerWithKey<String, DatastreamsSetter> datastreamsSetterRegistrationManager;
    private final MqttDatastreamsFactory mqttDatastreamsFactory;
    private final Map<String, MqttDatastreamsGetter> mqttDatastreamsGetters = new ConcurrentHashMap<>();
    private final Map<String, MqttDatastreamsSetter> mqttDatastreamsSetters = new ConcurrentHashMap<>();


    MqttDatastreamsManager(ServiceRegistrationManagerWithKey<String, DatastreamsGetter> datastreamsGetterRegistrationManager,
                           ServiceRegistrationManagerWithKey<String, DatastreamsSetter> datastreamsSetterRegistrationManager,
                           MqttDatastreamsFactory mqttDatastreamsFactory) {
        this.datastreamsGetterRegistrationManager = datastreamsGetterRegistrationManager;
        this.datastreamsSetterRegistrationManager = datastreamsSetterRegistrationManager;
        this.mqttDatastreamsFactory = mqttDatastreamsFactory;
    }

    void createDatastream(String deviceId, String datastreamId) throws MqttException {
        createDatastreamsGetter(deviceId, datastreamId);
        createDatastreamsSetter(deviceId, datastreamId);
    }

    @FunctionalInterface
    private interface ThrowableFunction <T, R, E extends Exception> {
        R apply(T arg) throws E;
    }

    private <T> void createDatastreamIfNotExists(String datastreamId, Map<String, T> datastreams,
                                                 ThrowableFunction<String, T, MqttException> datastreamCreator)
            throws MqttException {
        if (!datastreams.containsKey(datastreamId)) {
            datastreams.put(datastreamId, datastreamCreator.apply(datastreamId));
        }
    }

    private void createDatastreamsGetter(String deviceId, String datastreamId) throws MqttException {
        createDatastreamIfNotExists(datastreamId, mqttDatastreamsGetters, this::createAndRegisterDatastreamsGetter);
        MqttDatastreamsGetter getter = mqttDatastreamsGetters.get(datastreamId);
        getter.addManagedDevice(deviceId);
    }

    private MqttDatastreamsGetter createAndRegisterDatastreamsGetter(String datastreamId) throws MqttException {
        MqttDatastreamsGetter getter = mqttDatastreamsFactory.createDatastreamGetter(datastreamId);
        datastreamsGetterRegistrationManager.register(datastreamId, getter);
        return getter;
    }

    private void createDatastreamsSetter(String deviceId, String datastreamId) throws MqttException {
        createDatastreamIfNotExists(datastreamId, mqttDatastreamsSetters, this::createAndRegisterDatastreamsSetter);
        MqttDatastreamsSetter setter = mqttDatastreamsSetters.get(datastreamId);
        setter.addManagedDevice(deviceId);
    }

    private MqttDatastreamsSetter createAndRegisterDatastreamsSetter(String datastreamId) throws MqttException {
        MqttDatastreamsSetter setter = mqttDatastreamsFactory.createDatastreamSetter(datastreamId);
        datastreamsSetterRegistrationManager.register(datastreamId, setter);
        return setter;
    }

    void removeDatastream(String deviceId, String datastreamId) {
        removeDatastreamsGetter(deviceId, datastreamId);
        removeDatastreamsSetter(deviceId, datastreamId);
    }

    private void removeDatastreamsGetter(String deviceId, String datastreamId) {
        if (mqttDatastreamsGetters.containsKey(datastreamId)) {
            MqttDatastreamsGetter getter = mqttDatastreamsGetters.get(datastreamId);
            getter.removeManagedDevice(deviceId);
            if (getter.getDevicesIdManaged().isEmpty()) {
                getter.close();
                datastreamsGetterRegistrationManager.unregister(datastreamId);
                mqttDatastreamsGetters.remove(datastreamId);
            }
        }
    }

    private void removeDatastreamsSetter(String deviceId, String datastreamId) {
        if (mqttDatastreamsSetters.containsKey(datastreamId)) {
            MqttDatastreamsSetter setter = mqttDatastreamsSetters.get(datastreamId);
            setter.removeManagedDevice(deviceId);
            if (setter.getDevicesIdManaged().isEmpty()) {
                setter.close();
                datastreamsSetterRegistrationManager.unregister(datastreamId);
                mqttDatastreamsSetters.remove(datastreamId);
            }
        }
    }

    void removeDevice(String deviceId) {
        mqttDatastreamsGetters.values().stream()
                .filter(getter -> getter.getDevicesIdManaged().contains(deviceId))
                .forEach(getter -> removeDatastream(deviceId, getter.getDatastreamIdSatisfied()));
        mqttDatastreamsSetters.values().stream()
                .filter(setter -> setter.getDevicesIdManaged().contains(deviceId))
                .forEach(setter -> removeDatastream(deviceId, setter.getDatastreamIdSatisfied()));
    }

    @Override
    public void close() {
        datastreamsGetterRegistrationManager.unregisterAll();
        datastreamsSetterRegistrationManager.unregisterAll();
        mqttDatastreamsGetters.values().forEach(MqttDatastreamsGetter::close);
        mqttDatastreamsGetters.clear();
        mqttDatastreamsSetters.values().forEach(MqttDatastreamsSetter::close);
        mqttDatastreamsSetters.clear();
    }
}
