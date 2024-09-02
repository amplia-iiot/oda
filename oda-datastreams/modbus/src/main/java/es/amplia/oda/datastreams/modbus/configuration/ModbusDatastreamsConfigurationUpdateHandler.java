package es.amplia.oda.datastreams.modbus.configuration;

import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbus.ModbusDatastreamsManager;
import es.amplia.oda.datastreams.modbus.ModbusType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;

public class ModbusDatastreamsConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusDatastreamsConfigurationUpdateHandler.class);

    private static final Map<String, Type> TYPES_MAPPER = generateTypesMapper();
    private static final Map<String, ModbusType> MODBUS_TYPES_MAPPER = generateModbusTypesMapper();
    private static final String DATASTREAM_TYPE_PROPERTY_NAME = "datastreamType";
    private static final String SLAVE_ADDRESS_PROPERTY_NAME = "slaveAddress";
    private static final String DATA_TYPE_PROPERTY_NAME = "dataType";
    private static final String DATA_ADDRESS_PROPERTY_NAME = "dataAddress";
    private static final String NUM_REGISTERS_PROPERTY_NAME = "numRegisters";
    private static final String READ_CACHE_PROPERTY_NAME = "readCache";


    private static final int KEY_FIELDS_SIZE = 2;
    private static final String KEY_FIELDS_DELIMITER = ";";


    private final ModbusDatastreamsManager modbusDatastreamsManager;
    private final List<ModbusDatastreamsConfiguration> currentModbusDatastreamsConfigurations = new ArrayList<>();


    public ModbusDatastreamsConfigurationUpdateHandler(ModbusDatastreamsManager modbusDatastreamsManager) {
        this.modbusDatastreamsManager = modbusDatastreamsManager;
    }

    private static Map<String, Type> generateTypesMapper() {
        Map<String, Type> mapper = new HashMap<>();
        mapper.put("bit", Boolean.class);
        mapper.put("bool", Boolean.class);
        mapper.put("boolean", Boolean.class);
        mapper.put("short", Short.class);
        mapper.put("int", Integer.class);
        mapper.put("integer", Integer.class);
        mapper.put("long", Long.class);
        mapper.put("float", Float.class);
        mapper.put("double", Double.class);
        mapper.put("bytes", Byte[].class);
        mapper.put("byteArray", Byte[].class);
        mapper.put("list", List.class);
        return mapper;
    }

    private static Map<String, ModbusType> generateModbusTypesMapper() {
        Map<String, ModbusType> mapper = new HashMap<>();
        mapper.put("inputdiscrete", ModbusType.INPUT_DISCRETE);
        mapper.put("input_discrete", ModbusType.INPUT_DISCRETE);
        mapper.put("coil", ModbusType.COIL);
        mapper.put("inputregister", ModbusType.INPUT_REGISTER);
        mapper.put("input_register", ModbusType.INPUT_REGISTER);
        mapper.put("holdingregister", ModbusType.HOLDING_REGISTER);
        mapper.put("holding_register", ModbusType.HOLDING_REGISTER);
        return mapper;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        currentModbusDatastreamsConfigurations.clear();

        Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
        for (Map.Entry<String, ?> entry : mappedProperties.entrySet()) {
            try {
                String[] keys = entry.getKey().split(KEY_FIELDS_DELIMITER);
                String datastreamId = keys[0];
                String deviceId = "";
                if (keys.length == KEY_FIELDS_SIZE) {
                    deviceId = keys[1];
                }

                ModbusDatastreamsConfiguration.ModbusDatastreamsConfigurationBuilder builder =
                        ModbusDatastreamsConfiguration.builder().datastreamId(datastreamId).deviceId(deviceId);
                String[] properties = getTokensFromProperty((String) entry.getValue());
                getValueByToken(DATASTREAM_TYPE_PROPERTY_NAME, properties)
                        .ifPresent(value -> builder.datastreamType(getDatastreamType(value)));
                getValueByToken(SLAVE_ADDRESS_PROPERTY_NAME, properties)
                        .ifPresent(value -> builder.slaveAddress(Integer.parseInt(value)));
                getValueByToken(DATA_TYPE_PROPERTY_NAME, properties)
                        .ifPresent(value -> builder.dataType(getModbusType(value)));
                getValueByToken(DATA_ADDRESS_PROPERTY_NAME, properties)
                        .ifPresent(value -> builder.dataAddress(Integer.parseInt(value)));
                getValueByToken(NUM_REGISTERS_PROPERTY_NAME, properties)
                        .ifPresent(value -> builder.numRegistersToRead(Integer.parseInt(value)));
                getValueByToken(READ_CACHE_PROPERTY_NAME, properties)
                        .ifPresent(value -> builder.readFromCache(Boolean.parseBoolean(value)));

                currentModbusDatastreamsConfigurations.add(builder.build());
            } catch (Exception exception) {
                LOGGER.error("Invalid modbus datastream configuration entry {}={}: {}", entry.getKey(),
                        entry.getValue(), exception.getMessage());
            }
        }
    }

    private Type getDatastreamType(String type) {
        return TYPES_MAPPER.getOrDefault(type.toLowerCase(), Object.class);
    }

    private ModbusType getModbusType(String modbusType) {
        return Optional.ofNullable(MODBUS_TYPES_MAPPER.get(modbusType.toLowerCase()))
                .orElseThrow(() -> new IllegalArgumentException("Illegal modbus data type"));
    }

    @Override
    public void loadDefaultConfiguration() {
        currentModbusDatastreamsConfigurations.clear();
    }

    @Override
    public void applyConfiguration() {
        modbusDatastreamsManager.loadConfiguration(currentModbusDatastreamsConfigurations);
    }
}
