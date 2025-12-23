package es.amplia.oda.datastreams.modbusslave.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbusslave.internal.ModbusSlaveManager;
import es.amplia.oda.datastreams.modbusslave.translator.ModbusEventTranslator;
import es.amplia.oda.datastreams.modbusslave.translator.TranslationEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class ModbusSlaveConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    public static final String TYPE_PROPERTY_NAME = "type";
    public static final String IP_PROPERTY_NAME = "ip";
    public static final String PORT_PROPERTY_NAME = "port";
    public static final String SLAVE_ADDRESS_PROPERTY_NAME = "slaveAddress";
    public static final String DATASTREAM_ID_PROPERTY_NAME = "datastream";
    public static final String FEED_PROPERTY_NAME = "feed";
    public static final String DATA_TYPE_PROPERTY_NAME = "dataType";
    public static final String NUM_VALUES_TO_GET_PROPERTY_NAME = "numValuesToGet";


    public static final String TCP_MODBUS_TYPE = "TCP";
    public static final String UDP_MODBUS_TYPE = "UDP";
    public static final String SERIAL_MODBUS_TYPE = "Serial";

    private final ModbusSlaveManager modbusSlaveManager;
    // map is <Type (TCP,UDP, SERIAL), list<ModbusSlave>>
    private final Map<String,List<Object>> currentModbusSlaveConfigurations = new HashMap<>();

    public ModbusSlaveConfigurationUpdateHandler(ModbusSlaveManager modbusSlaveManager) {
        this.modbusSlaveManager = modbusSlaveManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        currentModbusSlaveConfigurations.clear();
        ModbusEventTranslator.clearAllEntries();

        Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
        for (Map.Entry<String, ?> entry : mappedProperties.entrySet()) {
            try {
                // split key
                String[] keyProperties = getTokensFromProperty(entry.getKey());
                // split properties
                String[] properties = getTokensFromProperty((String) entry.getValue());

                // if there is only one element in key, it is a device
                // deviceId1=type:TCP,ip:127.0.0.1,port:5020,slaveAddress:3
                if (keyProperties.length == 1) {
                    loadConnectionConfiguration(keyProperties, properties);
                }
                // if there are two elements in key, it is a translation
                // 102, deviceId1 = datastream:ds1, feed:feed1, dataType:Float
                else if (keyProperties.length == 2) {
                    loadTranslationConfiguration(keyProperties, properties);
                }

            } catch (Exception exception) {
                logInvalidConfigurationWarning(entry, exception.getMessage());
            }
        }
    }

    @Override
    public void applyConfiguration() {
        this.modbusSlaveManager.loadConfiguration(currentModbusSlaveConfigurations);
    }

    private Supplier<RuntimeException> throwMissingRequiredPropertyConfigurationException(String propertyName) {
        return () -> new ConfigurationException("Missing required property \"" + propertyName + "\"");
    }

    private void logInvalidConfigurationWarning(Map.Entry<String, ?> entry, String message) {
        log.warn("Invalid configuration entry  \"{}\": {}", entry, message);
    }

    private void loadTranslationConfiguration(String[] keyProperties, String[] properties) {
        int modbusAddress = Integer.parseInt(keyProperties[0]);
        String deviceId = keyProperties[1];

        // get datastreamId
        String datastreamId = getValueByToken(DATASTREAM_ID_PROPERTY_NAME, properties)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(DATASTREAM_ID_PROPERTY_NAME));

        // get feed
        String feed = getValueByToken(FEED_PROPERTY_NAME, properties).orElse(null);

        // get datatype
        String dataType = getValueByToken(DATA_TYPE_PROPERTY_NAME, properties)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(DATA_TYPE_PROPERTY_NAME));

        // in case we want to return a list of registers
        String numValuesToGetString = getValueByToken(NUM_VALUES_TO_GET_PROPERTY_NAME, properties).orElse(null);
        Integer numValuesToGet = numValuesToGetString != null ? Integer.parseInt(numValuesToGetString) : null;

        TranslationEntry newEntry = new TranslationEntry(modbusAddress, deviceId, datastreamId, feed, dataType, numValuesToGet);
        ModbusEventTranslator.addEntry(newEntry);
    }

    private void loadConnectionConfiguration(String[] keyProperties, String[] properties) {
        String deviceId = keyProperties[0];

        // get type
        String type = getValueByToken(TYPE_PROPERTY_NAME, properties)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(TYPE_PROPERTY_NAME));

        if (type.equalsIgnoreCase(TCP_MODBUS_TYPE)) {
            loadTcpConfiguration(deviceId, properties);
        }
    }

    private void loadTcpConfiguration(String deviceId, String[] properties) {
        ModbusTCPSlaveConfiguration.ModbusTCPSlaveConfigurationBuilder builder = ModbusTCPSlaveConfiguration.builder();
        builder.deviceId(deviceId);

        builder.ipAddress(getValueByToken(IP_PROPERTY_NAME, properties)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(IP_PROPERTY_NAME)));
        builder.listenPort(Integer.parseInt(getValueByToken(PORT_PROPERTY_NAME, properties)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(PORT_PROPERTY_NAME))));
        builder.slaveAddress(Integer.parseInt(getValueByToken(SLAVE_ADDRESS_PROPERTY_NAME, properties)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(SLAVE_ADDRESS_PROPERTY_NAME))));

        // add new device to list
        List<Object> modbusSlaves = currentModbusSlaveConfigurations.get(TCP_MODBUS_TYPE);
        if (modbusSlaves == null) {
            modbusSlaves = new ArrayList<>();
        }
        modbusSlaves.add(builder.build());
        currentModbusSlaveConfigurations.put(TCP_MODBUS_TYPE, modbusSlaves);
    }
}
