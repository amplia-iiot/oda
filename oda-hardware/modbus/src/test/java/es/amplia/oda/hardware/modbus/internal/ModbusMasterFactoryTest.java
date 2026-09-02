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
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
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

    @Test
    public void testCreateTCPModbusMaster() throws Exception {
        List<List<?>> tcpMasterArgs = new ArrayList<>();

        try (MockedConstruction<ModbusTCPMaster> tcpMasterCons = mockConstruction(ModbusTCPMaster.class,
                (mock, mctx) -> tcpMasterArgs.add(new ArrayList<>(mctx.arguments())))) {

            testFactory.createTCPModbusMaster(TEST_TCP_CONFIGURATION);

            assertEquals(1, tcpMasterCons.constructed().size());
            assertEquals(Arrays.asList(TEST_ADDRESS, TEST_PORT, TEST_TIMEOUT, TEST_NEW_CONN_PER_REQUEST),
                    tcpMasterArgs.get(0));
        }
    }

    @Test
    public void testCreateUDPModbusMaster() throws Exception {
        List<List<?>> udpMasterArgs = new ArrayList<>();

        try (MockedConstruction<ModbusUDPMaster> udpMasterCons = mockConstruction(ModbusUDPMaster.class,
                (mock, mctx) -> udpMasterArgs.add(new ArrayList<>(mctx.arguments())))) {

            testFactory.createUDPModbusMaster(TEST_UDP_CONFIGURATION);

            assertEquals(1, udpMasterCons.constructed().size());
            assertEquals(Arrays.asList(TEST_ADDRESS, TEST_PORT, TEST_TIMEOUT), udpMasterArgs.get(0));
        }
    }

    @Test
    public void testCreateSerialModbusMaster() throws Exception {
        List<List<?>> serialParamsArgs = new ArrayList<>();
        List<List<?>> serialMasterArgs = new ArrayList<>();

        try (MockedConstruction<ModbusSerialMaster> serialMasterCons = mockConstruction(ModbusSerialMaster.class,
                (mock, mctx) -> serialMasterArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SerialParameters> serialParamsCons = mockConstruction(SerialParameters.class,
                     (mock, mctx) -> serialParamsArgs.add(new ArrayList<>(mctx.arguments())))) {

            testFactory.createSerialModbusMaster(TEST_SERIAL_CONFIGURATION);

            assertEquals(1, serialParamsCons.constructed().size());
            assertEquals(Arrays.asList(TEST_PORT_NAME, TEST_BAUD_RATE, TEST_FLOW_CONTROL_IN, TEST_FLOW_CONTROL_OUT,
                    TEST_DATA_BITS, TEST_STOP_BITS, TEST_PARITY, TEST_ECHO), serialParamsArgs.get(0));
            verify(serialParamsCons.constructed().get(0)).setEncoding(TEST_ENCODING);
            assertEquals(1, serialMasterCons.constructed().size());
            assertEquals(serialParamsCons.constructed().get(0), serialMasterArgs.get(0).get(0));
            assertEquals(TEST_TIMEOUT, serialMasterArgs.get(0).get(1));
        }
    }
}
