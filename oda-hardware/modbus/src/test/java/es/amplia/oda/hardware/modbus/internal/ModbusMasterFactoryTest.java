package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.hardware.modbus.configuration.SerialModbusConfiguration;
import es.amplia.oda.hardware.modbus.configuration.TCPModbusMasterConfiguration;
import es.amplia.oda.hardware.modbus.configuration.UDPModbusMasterConfiguration;

import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.facade.ModbusUDPMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ModbusMasterFactory.class)
public class ModbusMasterFactoryTest {

    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_PORT = 12345;
    private static final int TEST_TIMEOUT = 10000;
    private static final boolean TEST_NEW_CONN_PER_REQUEST = true;
    private static final String TEST_DEVICE_ID = "deviceId";
    private static final String TEST_PORT_NAME = "testPort";
    private static final int TEST_BAUD_RATE = 38400;
    private static final int TEST_FLOW_CONTROL_IN = 1;
    private static final int TEST_FLOW_CONTROL_OUT = 1;
    private static final int TEST_DATA_BITS = 16;
    private static final int TEST_STOP_BITS = 2;
    private static final int TEST_PARITY = 1;
    private static final boolean TEST_ECHO = true;
    private static final String TEST_ENCODING = "rtu";

    private static final TCPModbusMasterConfiguration TEST_TCP_CONFIGURATION =
            TCPModbusMasterConfiguration.builder().address(TEST_ADDRESS).port(TEST_PORT).timeout(TEST_TIMEOUT)
                    .newConnPerRequest(TEST_NEW_CONN_PER_REQUEST).deviceId(TEST_DEVICE_ID).build();
    private static final UDPModbusMasterConfiguration TEST_UDP_CONFIGURATION =
            UDPModbusMasterConfiguration.builder().address(TEST_ADDRESS).port(TEST_PORT).timeout(TEST_TIMEOUT)
                    .deviceId(TEST_DEVICE_ID).build();
    private static final SerialModbusConfiguration TEST_SERIAL_CONFIGURATION =
            SerialModbusConfiguration.builder().portName(TEST_PORT_NAME).deviceId(TEST_DEVICE_ID).baudRate(TEST_BAUD_RATE)
                    .flowControlIn(TEST_FLOW_CONTROL_IN).flowControlOut(TEST_FLOW_CONTROL_OUT)
                    .dataBits(TEST_DATA_BITS).stopBits(TEST_STOP_BITS).parity(TEST_PARITY).echo(TEST_ECHO)
                    .encoding(TEST_ENCODING).timeout(TEST_TIMEOUT).build();

    private final ModbusMasterFactory testFactory = new ModbusMasterFactory();

    @Mock
    private ModbusTCPMaster mockedTCPMaster;
    @Mock
    private ModbusUDPMaster mockedUDPMaster;
    @Mock
    private ModbusSerialMaster mockedSerialMaster;
    @Mock
    private SerialParameters mockedSerialParams;

    @Test
    public void testCreateTCPModbusMaster() throws Exception {
        PowerMockito.whenNew(ModbusTCPMaster.class).withAnyArguments().thenReturn(mockedTCPMaster);

        testFactory.createTCPModbusMaster(TEST_TCP_CONFIGURATION);

        PowerMockito.verifyNew(ModbusTCPMaster.class).withArguments(eq(TEST_ADDRESS), eq(TEST_PORT), eq(TEST_TIMEOUT),
                eq(TEST_NEW_CONN_PER_REQUEST));
    }

    @Test
    public void testCreateUDPModbusMaster() throws Exception {
        PowerMockito.whenNew(ModbusUDPMaster.class).withAnyArguments().thenReturn(mockedUDPMaster);

        testFactory.createUDPModbusMaster(TEST_UDP_CONFIGURATION);

        PowerMockito.verifyNew(ModbusUDPMaster.class).withArguments(eq(TEST_ADDRESS), eq(TEST_PORT), eq(TEST_TIMEOUT));
    }

    @Test
    public void testCreateSerialModbusMaster() throws Exception {
        PowerMockito.whenNew(ModbusSerialMaster.class).withAnyArguments().thenReturn(mockedSerialMaster);
        PowerMockito.whenNew(SerialParameters.class).withAnyArguments().thenReturn(mockedSerialParams);

        testFactory.createSerialModbusMaster(TEST_SERIAL_CONFIGURATION);

        PowerMockito.verifyNew(SerialParameters.class).withArguments(eq(TEST_PORT_NAME), eq(TEST_BAUD_RATE),
                eq(TEST_FLOW_CONTROL_IN), eq(TEST_FLOW_CONTROL_OUT), eq(TEST_DATA_BITS), eq(TEST_STOP_BITS),
                eq(TEST_PARITY), eq(TEST_ECHO));
        verify(mockedSerialParams).setEncoding(TEST_ENCODING);
        PowerMockito.verifyNew(ModbusSerialMaster.class).withArguments(eq(mockedSerialParams), eq(TEST_TIMEOUT));
    }
}