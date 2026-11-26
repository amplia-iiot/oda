package es.amplia.oda.datastreams.modbusslave.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.datastreams.modbusslave.internal.ModbusSlaveManager;
import es.amplia.oda.datastreams.modbusslave.translator.ModbusEventTranslator;
import es.amplia.oda.datastreams.modbusslave.translator.TranslationEntry;
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
import java.util.*;
import static es.amplia.oda.datastreams.modbusslave.configuration.ModbusSlaveConfigurationUpdateHandler.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ModbusSlaveConfigurationUpdateHandlerTest {

    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_PORT = 12345;
    private static final int TEST_SLAVE_ADDRESS = 1;
    private static final String TEST_DEVICE_ID = "deviceId";
    private static final String TEST_DATASTREAM_ID = "datastreamId";
    private static final String TEST_FEED_ID = "feedId";
    private static final String TEST_DATATYPE = "Short";
    private static final int TEST_START_MODBUS_ADDRESS = 254;
    private static final int TEST_END_MODBUS_ADDRESS = 286;


    private static final ModbusTCPDeviceConfiguration TEST_TCP_CONFIGURATION =
            ModbusTCPDeviceConfiguration.builder().ipAddress(TEST_ADDRESS).listenPort(TEST_PORT)
                    .deviceId(TEST_DEVICE_ID).slaveAddress(TEST_SLAVE_ADDRESS).build();

    private final Map<String, List<Object>> expectedModbusSlaves = new HashMap<>();


    @Mock
    private ModbusSlaveManager mockedModbusSlaveManager;
    @InjectMocks
    private ModbusSlaveConfigurationUpdateHandler testConfigHandler;

    @BeforeEach
    public void prepare() {
        // expected slave configuration
        ModbusTCPDeviceConfiguration expectedSlaveConfiguration = new ModbusTCPDeviceConfiguration(TEST_ADDRESS,
                TEST_PORT, TEST_DEVICE_ID, TEST_SLAVE_ADDRESS);
        expectedModbusSlaves.put(TCP_MODBUS_TYPE, Collections.singletonList(expectedSlaveConfiguration));
    }

    @Test
    public void testLoadTCPConfiguration() {
        Dictionary<String, String> config = new Hashtable<>();
        String valueString = TYPE_PROPERTY_NAME + ":" + TCP_MODBUS_TYPE + "," +
                IP_PROPERTY_NAME + ":" + TEST_ADDRESS + "," +
                PORT_PROPERTY_NAME + ":" + TEST_PORT + "," +
                SLAVE_ADDRESS_PROPERTY_NAME + ":" + TEST_SLAVE_ADDRESS;
        config.put(TEST_DEVICE_ID, valueString);

        // call load method to test
        testConfigHandler.loadConfiguration(config);

        // retrieve loaded modbus slave configuration
        Object currentModbusSlaves = Whitebox.getInternalState(testConfigHandler, "currentModbusSlaveConfigurations");

        // assertions
        Assertions.assertEquals(expectedModbusSlaves, currentModbusSlaves);
    }

    @Test
    public void testLoadTCPMissingPropertyConfiguration() {
        Dictionary<String, String> config = new Hashtable<>();
        String valueString = TYPE_PROPERTY_NAME + ":" + TCP_MODBUS_TYPE + "," +
                IP_PROPERTY_NAME + ":" + TEST_ADDRESS + "," +
                PORT_PROPERTY_NAME + ":" + TEST_PORT;
        config.put(TEST_DEVICE_ID, valueString);

        // call load method to test
        testConfigHandler.loadConfiguration(config);
    }

    @Test
    public void testLoadTranslationEntryConfiguration() {
        // clear all translation entries
        ModbusEventTranslator.clearAllEntries();

        // prepare configuration
        Dictionary<String, String> config = new Hashtable<>();

        String keyString = TEST_START_MODBUS_ADDRESS + "," + TEST_DEVICE_ID;
        String valueString = DATASTREAM_ID_PROPERTY_NAME + ":" + TEST_DATASTREAM_ID + "," +
                FEED_PROPERTY_NAME + ":" + TEST_FEED_ID + "," +
                DATA_TYPE_PROPERTY_NAME + ":" + TEST_DATATYPE;

        config.put(keyString, valueString);

        TranslationEntry expectedTranslationEntry = new TranslationEntry(TEST_START_MODBUS_ADDRESS, TEST_START_MODBUS_ADDRESS,
                TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE);

        // call load method to test
        testConfigHandler.loadConfiguration(config);

        // retrieve loaded translation entries
        List<TranslationEntry> translationEntries = ModbusEventTranslator.getExistingNonBlockTranslations(TEST_START_MODBUS_ADDRESS, TEST_DEVICE_ID);

        // assertions
        Assertions.assertEquals(expectedTranslationEntry, translationEntries.get(0));
    }

    @Test
    public void testLoadDefaultConfigurationNotAllowed() {
        assertThrows(ConfigurationException.class, () -> testConfigHandler.loadDefaultConfiguration());
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testConfigHandler, "currentModbusSlaveConfigurations", expectedModbusSlaves);

        // call method to test
        testConfigHandler.applyConfiguration();

        // assertions
        verify(mockedModbusSlaveManager).loadConfiguration(expectedModbusSlaves);
    }


}