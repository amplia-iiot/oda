package es.amplia.oda.datastreams.modbusslave.internal;


import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusTCPSlaveConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static es.amplia.oda.datastreams.modbusslave.configuration.ModbusSlaveConfigurationUpdateHandler.TCP_MODBUS_TYPE;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ModbusSlaveManager.class)
public class ModbusSlaveManagerTest {

    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_PORT = 12345;
    private static final int TEST_SLAVE_ADDRESS = 1;
    private static final String TEST_DEVICE_ID = "deviceId";

    @Mock
    StateManager mockedStateManager;
    @Mock
    ModbusTCPSlaveImpl mockedTcpSlaveImpl;

    @InjectMocks
    ModbusSlaveManager slaveManager = new ModbusSlaveManager(mockedStateManager);

    @Test
    public void loadConfigTest() throws Exception {

        // prepare config
        Map<String, List<Object>> modbusSlaves = new HashMap<>();
        List<Object> tcpSlavesConf = new ArrayList<>();
        ModbusTCPSlaveConfiguration.ModbusTCPSlaveConfigurationBuilder tcpConfigBuilder = ModbusTCPSlaveConfiguration.builder();
        tcpConfigBuilder.ipAddress(TEST_ADDRESS).slaveAddress(TEST_SLAVE_ADDRESS).listenPort(TEST_PORT).deviceId(TEST_DEVICE_ID);
        tcpSlavesConf.add(tcpConfigBuilder.build());
        modbusSlaves.put(TCP_MODBUS_TYPE, tcpSlavesConf);

        PowerMockito.whenNew(ModbusTCPSlaveImpl.class).withArguments(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.any(StateManager.class)).thenReturn(mockedTcpSlaveImpl);
        PowerMockito.doNothing().when(mockedTcpSlaveImpl).open();

        // call test
        slaveManager.loadConfiguration(modbusSlaves);

        // assertions
        verify(mockedTcpSlaveImpl, times(1)).open();
    }

    @Test
    public void closeSlaveTest() throws Exception {

        // prepare config
        Map<String, List<Object>> modbusSlaves = new HashMap<>();
        List<Object> tcpSlavesConf = new ArrayList<>();
        ModbusTCPSlaveConfiguration.ModbusTCPSlaveConfigurationBuilder tcpConfigBuilder = ModbusTCPSlaveConfiguration.builder();
        tcpConfigBuilder.ipAddress(TEST_ADDRESS).slaveAddress(TEST_SLAVE_ADDRESS).listenPort(TEST_PORT).deviceId(TEST_DEVICE_ID);
        tcpSlavesConf.add(tcpConfigBuilder.build());
        modbusSlaves.put(TCP_MODBUS_TYPE, tcpSlavesConf);

        PowerMockito.whenNew(ModbusTCPSlaveImpl.class).withArguments(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.any(StateManager.class)).thenReturn(mockedTcpSlaveImpl);
        PowerMockito.doNothing().when(mockedTcpSlaveImpl).open();

        // call test
        slaveManager.loadConfiguration(modbusSlaves);
        slaveManager.close();

        // assertions
        verify(mockedTcpSlaveImpl, times(1)).close();
    }
}
