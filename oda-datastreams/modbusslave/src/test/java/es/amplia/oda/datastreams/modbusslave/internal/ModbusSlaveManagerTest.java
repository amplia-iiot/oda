package es.amplia.oda.datastreams.modbusslave.internal;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusTCPDeviceConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static es.amplia.oda.datastreams.modbusslave.configuration.ModbusSlaveConfigurationUpdateHandler.TCP_MODBUS_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class ModbusSlaveManagerTest {

    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_PORT = 12345;
    private static final int TEST_SLAVE_ADDRESS = 1;
    private static final String TEST_DEVICE_ID = "deviceId";


    private static final ModbusTCPDeviceConfiguration TEST_TCP_CONFIGURATION =
            ModbusTCPDeviceConfiguration.builder().ipAddress(TEST_ADDRESS).listenPort(TEST_PORT)
                    .deviceId(TEST_DEVICE_ID).slaveAddress(TEST_SLAVE_ADDRESS).build();

    private final Map<String, List<Object>> modbusDevicesConfig = new HashMap<>();

    @Mock
    private StateManager mockedStateManager;
    @InjectMocks
    private ModbusSlaveManager modbusSlaveManager;

    @Before
    public void prepare() {
        modbusDevicesConfig.put(TCP_MODBUS_TYPE, Collections.singletonList(TEST_TCP_CONFIGURATION));
    }

    @Test
    public void loadConfigurationTest(){
        modbusSlaveManager.loadConfiguration(modbusDevicesConfig);

        // assertions
        Map<Integer, ModbusCustomTCPListener> modbusPortListeners = (Map<Integer, ModbusCustomTCPListener>)
                Whitebox.getInternalState(modbusSlaveManager, "modbusPortListeners");
        Assert.assertEquals(1, modbusPortListeners.size());
        Map<String, CustomModbusRequestHandlerTest> modbusRequestHandlers = (Map<String, CustomModbusRequestHandlerTest>)
                Whitebox.getInternalState(modbusSlaveManager, "modbusRequestHandlers");
        Assert.assertEquals(1, modbusRequestHandlers.size());
    }
}
