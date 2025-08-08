package es.amplia.oda.hardware.modbus.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterFactory;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

import static es.amplia.oda.hardware.modbus.configuration.ModbusMasterConfigurationUpdateHandler.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ModbusMasterConfigurationUpdateHandlerTest {

    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_PORT = 12345;
    private static final int TEST_TIMEOUT = 10000;
    private static final boolean TEST_NEW_CONN_PER_REQUEST = true;
    private static final String TEST_DEVICE_ID = "deviceId";
    private static final String TEST_DEVICE_MANUFACTURER = "testManufacturer";
    private static final String TEST_PORTS_SERIAL_PROPERTY = "testPort, deviceId";
    private static final String TEST_PORTS_SERIAL_PROPERTY_FULL = "testPort, deviceId, testManufacturer";
    private static final String TEST_CONNECT_PROPERTY_MIN = "deviceId, localhost";
    private static final String TEST_CONNECT_PROPERTY_FULL = "deviceId, localhost, 12345, testManufacturer";


    private static final TCPModbusMasterConfiguration TEST_TCP_COMPLETE_CONFIGURATION =
            TCPModbusMasterConfiguration.builder().address(TEST_ADDRESS).port(TEST_PORT).timeout(TEST_TIMEOUT)
                    .newConnPerRequest(TEST_NEW_CONN_PER_REQUEST).deviceId(TEST_DEVICE_ID).deviceManufacturer(TEST_DEVICE_MANUFACTURER)
                    .build();
    private static final TCPModbusMasterConfiguration TEST_TCP_DEFAULT_CONFIGURATION =
            TCPModbusMasterConfiguration.builder().address(TEST_ADDRESS).deviceId(TEST_DEVICE_ID).build();
    private static final UDPModbusMasterConfiguration TEST_UDP_COMPLETE_CONFIGURATION =
            UDPModbusMasterConfiguration.builder().address(TEST_ADDRESS).port(TEST_PORT).timeout(TEST_TIMEOUT)
                    .deviceId(TEST_DEVICE_ID).deviceManufacturer(TEST_DEVICE_MANUFACTURER).build();
    private static final UDPModbusMasterConfiguration TEST_UDP_DEFAULT_CONFIGURATION =
            UDPModbusMasterConfiguration.builder().address(TEST_ADDRESS).deviceId(TEST_DEVICE_ID).build();
    private static final String TEST_PORT_NAME = "testPort";
    private static final int TEST_BAUD_RATE = 38400;
    private static final int TEST_FLOW_CONTROL_IN = 1;
    private static final int TEST_FLOW_CONTROL_OUT = 1;
    private static final int TEST_DATA_BITS = 16;
    private static final int TEST_STOP_BITS = 2;
    private static final int TEST_PARITY = 1;
    private static final String TEST_ENCODING = "rtc";
    private static final boolean TEST_ECHO = true;
    private static final SerialModbusConfiguration TEST_SERIAL_COMPLETE_CONFIGURATION =
            SerialModbusConfiguration.builder().portName(TEST_PORT_NAME).deviceId(TEST_DEVICE_ID).baudRate(TEST_BAUD_RATE)
                    .flowControlIn(TEST_FLOW_CONTROL_IN).flowControlOut(TEST_FLOW_CONTROL_OUT).dataBits(TEST_DATA_BITS)
                    .stopBits(TEST_STOP_BITS).parity(TEST_PARITY).encoding(TEST_ENCODING).echo(TEST_ECHO)
                    .timeout(TEST_TIMEOUT).deviceManufacturer(TEST_DEVICE_MANUFACTURER).build();
    private static final SerialModbusConfiguration TEST_SERIAL_DEFAULT_CONFIGURATION =
            SerialModbusConfiguration.builder().portName(TEST_PORT_NAME).deviceId(TEST_DEVICE_ID).build();

    @Mock
    private ModbusMasterManager mockedModbusMasterManager;
    @Mock
    private ModbusMasterFactory mockedModbusMasterFactory;
    @InjectMocks
    private ModbusMasterConfigurationUpdateHandler testConfigHandler;

    @Mock
    private ModbusMaster mockedModbusMaster;

    @Test
    public void testLoadTCPCompleteConfiguration() {
        Dictionary<String, String>  tcpCompleteConfiguration = new Hashtable<>();
        tcpCompleteConfiguration.put(TYPE_PROPERTY_NAME, TCP_MODBUS_TYPE);
        tcpCompleteConfiguration.put(TIMEOUT_PROPERTY_NAME, Integer.toString(TEST_TIMEOUT));
        tcpCompleteConfiguration.put(NEW_CONNECTION_PER_REQUEST_PROPERTY_NAME, Boolean.toString(TEST_NEW_CONN_PER_REQUEST));
        tcpCompleteConfiguration.put(CONNECTIONS_PROPERTY_NAME, TEST_CONNECT_PROPERTY_FULL);

        testConfigHandler.loadConfiguration(tcpCompleteConfiguration);

        verify(mockedModbusMasterFactory).createTCPModbusMaster(TEST_TCP_COMPLETE_CONFIGURATION);
    }

    @Test
    public void testLoadTCPMinimumConfiguration() {
        Dictionary<String, String>  tcpRequiredConfiguration = new Hashtable<>();
        tcpRequiredConfiguration.put(TYPE_PROPERTY_NAME, TCP_MODBUS_TYPE);
        tcpRequiredConfiguration.put(CONNECTIONS_PROPERTY_NAME, TEST_CONNECT_PROPERTY_MIN);

        testConfigHandler.loadConfiguration(tcpRequiredConfiguration);

        verify(mockedModbusMasterFactory).createTCPModbusMaster(TEST_TCP_DEFAULT_CONFIGURATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadTCPInvalidConfiguration() {
        Dictionary<String, String>  tcpInvalidConfiguration = new Hashtable<>();
        tcpInvalidConfiguration.put(TYPE_PROPERTY_NAME, TCP_MODBUS_TYPE);
        tcpInvalidConfiguration.put(PORT_PROPERTY_NAME, Integer.toString(TEST_PORT));
        tcpInvalidConfiguration.put(TIMEOUT_PROPERTY_NAME, Integer.toString(TEST_TIMEOUT));
        tcpInvalidConfiguration.put(NEW_CONNECTION_PER_REQUEST_PROPERTY_NAME, Boolean.toString(TEST_NEW_CONN_PER_REQUEST));

        testConfigHandler.loadConfiguration(tcpInvalidConfiguration);

        verify(mockedModbusMasterFactory).createTCPModbusMaster(TEST_TCP_COMPLETE_CONFIGURATION);
    }

    @Test
    public void testLoadUDPCompleteConfiguration() {
        Dictionary<String, String>  udpCompleteConfiguration = new Hashtable<>();
        udpCompleteConfiguration.put(TYPE_PROPERTY_NAME, UDP_MODBUS_TYPE);
        udpCompleteConfiguration.put(TIMEOUT_PROPERTY_NAME, Integer.toString(TEST_TIMEOUT));
        udpCompleteConfiguration.put(CONNECTIONS_PROPERTY_NAME, TEST_CONNECT_PROPERTY_FULL);

        testConfigHandler.loadConfiguration(udpCompleteConfiguration);

        verify(mockedModbusMasterFactory).createUDPModbusMaster(TEST_UDP_COMPLETE_CONFIGURATION);
    }

    @Test
    public void testLoadUDPMinimumConfiguration() {
        Dictionary<String, String>  udpRequiredConfiguration = new Hashtable<>();
        udpRequiredConfiguration.put(TYPE_PROPERTY_NAME, UDP_MODBUS_TYPE);
        udpRequiredConfiguration.put(CONNECTIONS_PROPERTY_NAME, TEST_CONNECT_PROPERTY_MIN);

        testConfigHandler.loadConfiguration(udpRequiredConfiguration);

        verify(mockedModbusMasterFactory).createUDPModbusMaster(TEST_UDP_DEFAULT_CONFIGURATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadUDPInvalidConfiguration() {
        Dictionary<String, String>  udpInvalidConfiguration = new Hashtable<>();
        udpInvalidConfiguration.put(TYPE_PROPERTY_NAME, UDP_MODBUS_TYPE);
        udpInvalidConfiguration.put(PORT_PROPERTY_NAME, Integer.toString(TEST_PORT));
        udpInvalidConfiguration.put(TIMEOUT_PROPERTY_NAME, Integer.toString(TEST_TIMEOUT));
        udpInvalidConfiguration.put(DEVICE_ID, TEST_DEVICE_ID);

        testConfigHandler.loadConfiguration(udpInvalidConfiguration);
    }

    @Test
    public void testLoadSerialCompleteConfiguration() {
        Dictionary<String, String>  serialCompleteConfiguration = new Hashtable<>();
        serialCompleteConfiguration.put(TYPE_PROPERTY_NAME, SERIAL_MODBUS_TYPE);
        serialCompleteConfiguration.put(PORTS_PROPERTY_NAME, TEST_PORTS_SERIAL_PROPERTY_FULL);
        serialCompleteConfiguration.put(BAUD_RATE_PROPERTY_NAME, Integer.toString(TEST_BAUD_RATE));
        serialCompleteConfiguration.put(FLOW_CONTROL_IN_PROPERTY_NAME, Integer.toString(TEST_FLOW_CONTROL_IN));
        serialCompleteConfiguration.put(FLOW_CONTROL_OUT_PROPERTY_NAME, Integer.toString(TEST_FLOW_CONTROL_OUT));
        serialCompleteConfiguration.put(DATA_BITS_PROPERTY_NAME, Integer.toString(TEST_DATA_BITS));
        serialCompleteConfiguration.put(STOP_BITS_PROPERTY_NAME, Integer.toString(TEST_STOP_BITS));
        serialCompleteConfiguration.put(PARITY_PROPERTY_NAME, Integer.toString(TEST_PARITY));
        serialCompleteConfiguration.put(ENCODING_PROPERTY_NAME, TEST_ENCODING);
        serialCompleteConfiguration.put(ECHO_PROPERTY_NAME, Boolean.toString(TEST_ECHO));
        serialCompleteConfiguration.put(TIMEOUT_PROPERTY_NAME, Integer.toString(TEST_TIMEOUT));

        testConfigHandler.loadConfiguration(serialCompleteConfiguration);

        verify(mockedModbusMasterFactory).createSerialModbusMaster(TEST_SERIAL_COMPLETE_CONFIGURATION);
    }

    @Test
    public void testLoadSerialMinimumConfiguration() {
        Dictionary<String, String>  serialRequiredConfiguration = new Hashtable<>();
        serialRequiredConfiguration.put(TYPE_PROPERTY_NAME, SERIAL_MODBUS_TYPE);
        serialRequiredConfiguration.put(PORTS_PROPERTY_NAME, TEST_PORTS_SERIAL_PROPERTY);

        testConfigHandler.loadConfiguration(serialRequiredConfiguration);

        verify(mockedModbusMasterFactory).createSerialModbusMaster(TEST_SERIAL_DEFAULT_CONFIGURATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadSerialInvalidConfiguration() {
        Dictionary<String, String>  serialInvalidConfiguration = new Hashtable<>();
        serialInvalidConfiguration.put(TYPE_PROPERTY_NAME, SERIAL_MODBUS_TYPE);
        serialInvalidConfiguration.put(BAUD_RATE_PROPERTY_NAME, Integer.toString(TEST_BAUD_RATE));
        serialInvalidConfiguration.put(FLOW_CONTROL_IN_PROPERTY_NAME, Integer.toString(TEST_FLOW_CONTROL_IN));
        serialInvalidConfiguration.put(FLOW_CONTROL_OUT_PROPERTY_NAME, Integer.toString(TEST_FLOW_CONTROL_OUT));
        serialInvalidConfiguration.put(DATA_BITS_PROPERTY_NAME, Integer.toString(TEST_DATA_BITS));
        serialInvalidConfiguration.put(STOP_BITS_PROPERTY_NAME, Integer.toString(TEST_STOP_BITS));
        serialInvalidConfiguration.put(PARITY_PROPERTY_NAME, Integer.toString(TEST_PARITY));
        serialInvalidConfiguration.put(ENCODING_PROPERTY_NAME, TEST_ENCODING);
        serialInvalidConfiguration.put(ECHO_PROPERTY_NAME, Boolean.toString(TEST_ECHO));

        testConfigHandler.loadConfiguration(serialInvalidConfiguration);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationInvalidConfiguration() {
        Dictionary<String, String>  invalidConfiguration = new Hashtable<>();
        invalidConfiguration.put(TYPE_PROPERTY_NAME, "invalidType");

        testConfigHandler.loadConfiguration(invalidConfiguration);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadDefaultConfigurationNotAllowed() {
        testConfigHandler.loadDefaultConfiguration();
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testConfigHandler, "currentConfiguredModbusMaster", Collections.singletonList(mockedModbusMaster));

        testConfigHandler.applyConfiguration();

        verify(mockedModbusMasterManager).loadConfiguration(Collections.singletonList(mockedModbusMaster));
    }
}