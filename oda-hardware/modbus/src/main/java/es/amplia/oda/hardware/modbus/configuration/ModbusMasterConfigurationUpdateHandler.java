package es.amplia.oda.hardware.modbus.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterFactory;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterManager;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ModbusMasterConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    static final String TYPE_PROPERTY_NAME = "type";
    static final String ADDRESS_PROPERTY_NAME = "address";
    static final String PORT_PROPERTY_NAME = "port";
    static final String TIMEOUT_PROPERTY_NAME = "timeout";
    static final String RECONNECT_PROPERTY_NAME = "reconnect";
    static final String PORT_NAME_PROPERTY_NAME = "portName";
    static final String BAUD_RATE_PROPERTY_NAME = "baudRate";
    static final String FLOW_CONTROL_IN_PROPERTY_NAME = "flowControlIn";
    static final String FLOW_CONTROL_OUT_PROPERTY_NAME = "flowControlOut";
    static final String DATA_BITS_PROPERTY_NAME = "databits";
    static final String STOP_BITS_PROPERTY_NAME = "stopbits";
    static final String PARITY_PROPERTY_NAME = "parity";
    static final String ENCODING_PROPERTY_NAME = "encoding";
    static final String ECHO_PROPERTY_NAME = "echo";

    static final String TCP_MODBUS_TYPE = "TCP";
    static final String UDP_MODBUS_TYPE = "UDP";
    static final String SERIAL_MODBUS_TYPE = "Serial";


    private final ModbusMasterManager modbusMasterManager;
    private final ModbusMasterFactory modbusMasterFactory;
    private final Map<String, Consumer<Dictionary<String, ?>>> configuratorConsumers = new HashMap<>();
    private ModbusMaster currentConfiguredModbusMaster;

    public ModbusMasterConfigurationUpdateHandler(ModbusMasterManager modbusMasterManager,
                                                  ModbusMasterFactory modbusMasterFactory) {
        this.modbusMasterManager = modbusMasterManager;
        this.modbusMasterFactory = modbusMasterFactory;
        prepareConfiguratorConsumers();
    }

    private void prepareConfiguratorConsumers() {
        configuratorConsumers.put(TCP_MODBUS_TYPE, this::loadTCPConfiguration);
        configuratorConsumers.put(UDP_MODBUS_TYPE, this::loadUDPConfiguration);
        configuratorConsumers.put(SERIAL_MODBUS_TYPE, this::loadSerialConfiguration);
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        String type = Optional.ofNullable((String) props.get(TYPE_PROPERTY_NAME)).orElse("");
        Consumer<Dictionary<String, ?>> configuratorConsumer =
                Optional.ofNullable(configuratorConsumers.get(type))
                        .orElseThrow(() -> new ConfigurationException("Invalid Modbus type"));
        configuratorConsumer.accept(props);
    }

    private void loadTCPConfiguration(Dictionary<String, ?> props) {
        TCPModbusMasterConfiguration.TCPModbusMasterConfigurationBuilder builder =
                TCPModbusMasterConfiguration.builder();

        Optional.ofNullable((String) props.get(ADDRESS_PROPERTY_NAME)).ifPresent(builder::address);
        Optional.ofNullable((String) props.get(PORT_PROPERTY_NAME)).ifPresent(value ->
                builder.port(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(TIMEOUT_PROPERTY_NAME)).ifPresent(value ->
                builder.timeout(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(RECONNECT_PROPERTY_NAME)).ifPresent(value ->
                builder.reconnect(Boolean.parseBoolean(value)));

        currentConfiguredModbusMaster = modbusMasterFactory.createTCPModbusMaster(builder.build());
    }

    private void loadUDPConfiguration(Dictionary<String, ?> props) {
        UDPModbusMasterConfiguration.UDPModbusMasterConfigurationBuilder builder =
                UDPModbusMasterConfiguration.builder();

        Optional.ofNullable((String) props.get(ADDRESS_PROPERTY_NAME)).ifPresent(builder::address);
        Optional.ofNullable((String) props.get(PORT_PROPERTY_NAME)).ifPresent(value ->
                builder.port(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(TIMEOUT_PROPERTY_NAME)).ifPresent(value ->
                builder.timeout(Integer.parseInt(value)));

        currentConfiguredModbusMaster = modbusMasterFactory.createUDPModbusMaster(builder.build());
    }

    private void loadSerialConfiguration(Dictionary<String, ?> props) {
        SerialModbusConfiguration.SerialModbusConfigurationBuilder builder = SerialModbusConfiguration.builder();

        Optional.ofNullable((String) props.get(PORT_NAME_PROPERTY_NAME)).ifPresent(builder::portName);
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

        currentConfiguredModbusMaster = modbusMasterFactory.createSerialModbusMaster(builder.build());
    }

    @Override
    public void applyConfiguration() {
        modbusMasterManager.loadConfiguration(currentConfiguredModbusMaster);
    }
}
