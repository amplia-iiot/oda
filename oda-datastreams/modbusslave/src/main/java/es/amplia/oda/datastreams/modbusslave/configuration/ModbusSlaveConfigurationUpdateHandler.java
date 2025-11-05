package es.amplia.oda.datastreams.modbusslave.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbusslave.internal.ModbusSlaveManager;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class ModbusSlaveConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final String TYPE_PROPERTY_NAME = "type";
    private static final String IP_PROPERTY_NAME = "ip";
    private static final String PORT_PROPERTY_NAME = "port";
    private static final String SLAVE_ADDRESS_PROPERTY_NAME = "slaveAddress";

    public static final String TCP_MODBUS_TYPE = "TCP";
    public static final String UDP_MODBUS_TYPE = "UDP";
    public static final String SERIAL_MODBUS_TYPE = "Serial";

    private final ModbusSlaveManager modbusSlaveManager;
    private final Map<String,List<Object>> currentModbusSlaveConfigurations = new HashMap<>();

    public ModbusSlaveConfigurationUpdateHandler(ModbusSlaveManager modbusSlaveManager) {
        this.modbusSlaveManager = modbusSlaveManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        currentModbusSlaveConfigurations.clear();

        Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
        // deviceId1=type:TCP,ip:127.0.0.1,port:5020,slaveAddress:3

        for (Map.Entry<String, ?> entry : mappedProperties.entrySet()) {
            try {
                String deviceId = entry.getKey();
                // split properties
                String[] properties = getTokensFromProperty((String) entry.getValue());

                // get type
                String type = getValueByToken(TYPE_PROPERTY_NAME, properties)
                        .orElseThrow(throwMissingRequiredPropertyConfigurationException(TYPE_PROPERTY_NAME));

                if(type.equalsIgnoreCase(TCP_MODBUS_TYPE)) {
                    loadTcpConfiguration(deviceId, properties);
                }

            } catch (Exception exception) {
                logInvalidConfigurationWarning(entry, exception.getMessage());
            }
        }
    }

    @Override
    public void loadDefaultConfiguration() {
        currentModbusSlaveConfigurations.clear();
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
