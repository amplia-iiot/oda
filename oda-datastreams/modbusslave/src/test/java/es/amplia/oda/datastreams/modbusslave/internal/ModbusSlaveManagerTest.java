package es.amplia.oda.datastreams.modbusslave.internal;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusTCPDeviceConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static es.amplia.oda.datastreams.modbusslave.configuration.ModbusSlaveConfigurationUpdateHandler.TCP_MODBUS_TYPE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    public void prepare() {
        modbusDevicesConfig.put(TCP_MODBUS_TYPE, Collections.singletonList(TEST_TCP_CONFIGURATION));
    }

    @Test
    public void loadConfigurationTest(){
        modbusSlaveManager.loadConfiguration(modbusDevicesConfig);

        // assertions
        Map<Integer, ModbusCustomTCPListener> modbusPortListeners = (Map<Integer, ModbusCustomTCPListener>)
                Whitebox.getInternalState(modbusSlaveManager, "modbusPortListeners");
        Assertions.assertEquals(1, modbusPortListeners.size());
        Map<String, CustomModbusRequestHandlerTest> modbusRequestHandlers = (Map<String, CustomModbusRequestHandlerTest>)
                Whitebox.getInternalState(modbusSlaveManager, "modbusRequestHandlers");
        Assertions.assertEquals(1, modbusRequestHandlers.size());
    }
}
