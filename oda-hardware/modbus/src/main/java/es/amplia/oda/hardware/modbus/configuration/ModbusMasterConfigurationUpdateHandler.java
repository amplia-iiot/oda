package es.amplia.oda.hardware.modbus.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterFactory;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

public class ModbusMasterConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusMasterConfigurationUpdateHandler.class);

    static final String TYPE_PROPERTY_NAME = "type";
    static final String PORT_PROPERTY_NAME = "port";
    static final String TIMEOUT_PROPERTY_NAME = "timeout";
    static final String NEW_CONNECTION_PER_REQUEST_PROPERTY_NAME = "newConnPerRequest";
    static final String PORTS_PROPERTY_NAME = "ports";
    static final String BAUD_RATE_PROPERTY_NAME = "baudRate";
    static final String FLOW_CONTROL_IN_PROPERTY_NAME = "flowControlIn";
    static final String FLOW_CONTROL_OUT_PROPERTY_NAME = "flowControlOut";
    static final String DATA_BITS_PROPERTY_NAME = "databits";
    static final String STOP_BITS_PROPERTY_NAME = "stopbits";
    static final String PARITY_PROPERTY_NAME = "parity";
    static final String ENCODING_PROPERTY_NAME = "encoding";
    static final String ECHO_PROPERTY_NAME = "echo";
    static final String CONNECTIONS_PROPERTY_NAME = "connections";
    static final String DEVICE_ID = "deviceId";
    static final String TCP_MODBUS_TYPE = "TCP";
    static final String UDP_MODBUS_TYPE = "UDP";
    static final String SERIAL_MODBUS_TYPE = "Serial";


    private final ModbusMasterManager modbusMasterManager;
    private final ModbusMasterFactory modbusMasterFactory;
    private final Map<String, Consumer<Dictionary<String, ?>>> configuratorConsumers = new HashMap<>();
    private final List<ModbusMaster> currentConfiguredModbusMaster;

    public ModbusMasterConfigurationUpdateHandler(ModbusMasterManager modbusMasterManager,
                                                  ModbusMasterFactory modbusMasterFactory) {
        this.modbusMasterManager = modbusMasterManager;
        this.modbusMasterFactory = modbusMasterFactory;
        this.currentConfiguredModbusMaster = new ArrayList<>();
        prepareConfiguratorConsumers();
    }

    private void prepareConfiguratorConsumers() {
        configuratorConsumers.put(TCP_MODBUS_TYPE, this::loadTCPConfiguration);
        configuratorConsumers.put(UDP_MODBUS_TYPE, this::loadUDPConfiguration);
        configuratorConsumers.put(SERIAL_MODBUS_TYPE, this::loadSerialConfiguration);
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration");
        String type = Optional.ofNullable((String) props.get(TYPE_PROPERTY_NAME)).orElse("");
        Consumer<Dictionary<String, ?>> configuratorConsumer =
                Optional.ofNullable(configuratorConsumers.get(type))
                        .orElseThrow(() -> new ConfigurationException("Invalid Modbus type"));
        configuratorConsumer.accept(props);
        LOGGER.info("New configuration loaded");
    }

    private void loadTCPConfiguration(Dictionary<String, ?> props) {
        currentConfiguredModbusMaster.clear();

        // create builder
        TCPModbusMasterConfiguration.TCPModbusMasterConfigurationBuilder builder =
                TCPModbusMasterConfiguration.builder();

        // list of connections
        List<String> connectionList = new ArrayList<>();

        Optional.ofNullable((String) props.get(CONNECTIONS_PROPERTY_NAME)).ifPresent(connections ->
                connectionList.addAll(Arrays.asList(connections.split(";"))));

        if(connectionList.isEmpty()) {
            throw new IllegalArgumentException("No connections valid specified");
        }
        for (String item : connectionList) {

            // first element of string is the name of the connection
            // second element is the IP address
            // third element is the port
            // fourth element is the device manufacturer (bitronic, saci, etc.)
            List<String> connectionProperties = Arrays.asList(item.split(","));

            if (!connectionProperties.isEmpty()) {
                Optional.of(connectionProperties.get(0).trim()).ifPresent(builder::deviceId);
            }
            if (connectionProperties.size() >= 2) {
                Optional.of(connectionProperties.get(1).trim()).ifPresent(builder::address);
            }
            if (connectionProperties.size() >= 3) {
                Optional.of(connectionProperties.get(2).trim()).ifPresent(value ->
                        builder.port(Integer.parseInt(value)));
            }
            if (connectionProperties.size() >= 4) {
                Optional.of(connectionProperties.get(3).trim()).ifPresent(builder::deviceManufacturer);
            }

            Optional.ofNullable((String) props.get(TIMEOUT_PROPERTY_NAME)).ifPresent(value ->
                    builder.timeout(Integer.parseInt(value)));

            // NEW CONNECTION PER REQUEST
            // when value is true, a new modbus connection is created with every modbus request
            // the connection is closed when the reading/writing is done
            // when value is false, the same modbus connection is maintained all the time
            Optional.ofNullable((String) props.get(NEW_CONNECTION_PER_REQUEST_PROPERTY_NAME)).ifPresent(value ->
                    builder.newConnPerRequest(Boolean.parseBoolean(value)));

            currentConfiguredModbusMaster.add(modbusMasterFactory.createTCPModbusMaster(builder.build()));
        }
    }

    private void loadUDPConfiguration(Dictionary<String, ?> props) {
        currentConfiguredModbusMaster.clear();

        // create builder
        UDPModbusMasterConfiguration.UDPModbusMasterConfigurationBuilder builder =
                UDPModbusMasterConfiguration.builder();

        // list of connections
        List<String> connectionList = new ArrayList<>();

        Optional.ofNullable((String) props.get(CONNECTIONS_PROPERTY_NAME)).ifPresent(connections ->
                connectionList.addAll(Arrays.asList(connections.split(";"))));
        if(connectionList.isEmpty()) {
            throw new IllegalArgumentException("No connections valid specified");
        }
        for (String item : connectionList) {

            // first element of string is the device id
            // second element is the IP address
            // third element is the port
            // fourth element is the device manufacturer (bitronic, saci, etc.)
            List<String> connectionProperties = Arrays.asList(item.split(","));

            if (!connectionProperties.isEmpty()) {
                Optional.of(connectionProperties.get(0).trim()).ifPresent(builder::deviceId);
            }
            if (connectionProperties.size() >= 2) {
                Optional.of(connectionProperties.get(1).trim()).ifPresent(builder::address);
            }
            if (connectionProperties.size() >= 3) {
                Optional.of(connectionProperties.get(2).trim()).ifPresent(value ->
                        builder.port(Integer.parseInt(value)));
            }
            if (connectionProperties.size() >= 4) {
                Optional.of(connectionProperties.get(3).trim()).ifPresent(builder::deviceManufacturer);
            }

            Optional.ofNullable((String) props.get(TIMEOUT_PROPERTY_NAME)).ifPresent(value ->
                    builder.timeout(Integer.parseInt(value)));

            currentConfiguredModbusMaster.add(modbusMasterFactory.createUDPModbusMaster(builder.build()));
        }
    }

    private void loadSerialConfiguration(Dictionary<String, ?> props) {
        SerialModbusConfiguration.SerialModbusConfigurationBuilder builder = SerialModbusConfiguration.builder();
        List<String> portsInfo = new ArrayList<>();

        Optional.ofNullable((String) props.get(PORTS_PROPERTY_NAME)).ifPresent(ports ->
                portsInfo.addAll(Arrays.asList(ports.split(";"))));
        if(portsInfo.isEmpty()) {
            throw new IllegalArgumentException("No ports valid specified");
        }

        for (String item : portsInfo) {

            // first element of string is the port name
            // second element is the deviceId associated to the port
            // third element is the device manufacturer (bitronic, saci, etc.)
            List<String> portInfo = Arrays.asList(item.split(","));

            if (!portInfo.isEmpty()) {
                Optional.of(portInfo.get(0).trim()).ifPresent(builder::portName);
            }
            if (portInfo.size() >= 2) {
                Optional.of(portInfo.get(1).trim()).ifPresent(builder::deviceId);
            }
            if (portInfo.size() >= 3) {
                Optional.of(portInfo.get(2).trim()).ifPresent(builder::deviceManufacturer);
            }

            Optional.ofNullable((String) props.get(BAUD_RATE_PROPERTY_NAME)).ifPresent(value ->
                    builder.baudRate(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(FLOW_CONTROL_IN_PROPERTY_NAME)).ifPresent(value ->
                    builder.flowControlIn(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(FLOW_CONTROL_OUT_PROPERTY_NAME)).ifPresent(value ->
                    builder.flowControlOut(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(DATA_BITS_PROPERTY_NAME)).ifPresent(value ->
                    builder.dataBits(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(STOP_BITS_PROPERTY_NAME)).ifPresent(value ->
                    builder.stopBits(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(PARITY_PROPERTY_NAME)).ifPresent(value ->
                    builder.parity(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(ENCODING_PROPERTY_NAME)).ifPresent(builder::encoding);
            Optional.ofNullable((String) props.get(ECHO_PROPERTY_NAME)).ifPresent(value ->
                    builder.echo(Boolean.parseBoolean(value)));
            Optional.ofNullable((String) props.get(TIMEOUT_PROPERTY_NAME)).ifPresent(value ->
                    builder.timeout(Integer.parseInt(value)));

            currentConfiguredModbusMaster.add(modbusMasterFactory.createSerialModbusMaster(builder.build()));
        }
    }

    @Override
    public void applyConfiguration() {
        modbusMasterManager.loadConfiguration(currentConfiguredModbusMaster);
    }
}
