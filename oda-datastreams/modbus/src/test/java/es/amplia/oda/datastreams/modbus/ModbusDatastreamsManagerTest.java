package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import es.amplia.oda.datastreams.modbus.configuration.ModbusDatastreamsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Type;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModbusDatastreamsManagerTest {

    private static final String TEST_DATASTREAM_ID_1 = "testDatastream1";
    private static final Type TEST_DATASTREAM_TYPE_1 = Integer.class;
    private static final String TEST_DEVICE_ID_1 = "testDevice1";
    private static final int TEST_SLAVE_ADDRESS_1 = 1;
    private static final String TEST_DEVICE_ID_2 = "testDevice2";
    private static final int TEST_SLAVE_ADDRESS_2 = 2;
    private static final ModbusType TEST_DATA_TYPE_1 = ModbusType.INPUT_REGISTER;
    private static final int TEST_DATA_ADDRESS_1 = 5;
    private static final String TEST_DATASTREAM_ID_3 = "testDatastream2";
    private static final Type TEST_DATASTREAM_TYPE_3 = Double.class;
    private static final String TEST_DEVICE_ID_3 = "testDevice3";
    private static final int TEST_SLAVE_ADDRESS_3 = 3;
    private static final ModbusType TEST_DATA_TYPE_3 = ModbusType.HOLDING_REGISTER;
    private static final int TEST_DATA_ADDRESS_3 = 1;


    @Mock
    private ModbusDatastreamsFactory mockedFactory;
    @Mock
    private ServiceRegistrationManager mockedRegistrationManager;
    @InjectMocks
    private ModbusDatastreamsManager testModbusDatastreamsManager;

    @Mock
    private DatastreamsGetter mockedDatastreamsGetter;
    @Mock
    private DatastreamsSetter mockedDatastreamsSetter;

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadConfiguration() {
        ModbusDatastreamsConfiguration conf1 = ModbusDatastreamsConfiguration.builder()
                .datastreamId(TEST_DATASTREAM_ID_1).datastreamType(TEST_DATASTREAM_TYPE_1).deviceId(TEST_DEVICE_ID_1)
                .slaveAddress(TEST_SLAVE_ADDRESS_1).dataType(TEST_DATA_TYPE_1).dataAddress(TEST_DATA_ADDRESS_1).build();
        ModbusDatastreamsConfiguration conf2 = ModbusDatastreamsConfiguration.builder()
                .datastreamId(TEST_DATASTREAM_ID_1).datastreamType(TEST_DATASTREAM_TYPE_1).deviceId(TEST_DEVICE_ID_2)
                .slaveAddress(TEST_SLAVE_ADDRESS_2).dataType(TEST_DATA_TYPE_1).dataAddress(TEST_DATA_ADDRESS_1).build();
        ModbusDatastreamsConfiguration conf3 = ModbusDatastreamsConfiguration.builder()
                .datastreamId(TEST_DATASTREAM_ID_3).datastreamType(TEST_DATASTREAM_TYPE_3).deviceId(TEST_DEVICE_ID_3)
                .slaveAddress(TEST_SLAVE_ADDRESS_3).dataType(TEST_DATA_TYPE_3).dataAddress(TEST_DATA_ADDRESS_3).build();
        List<ModbusDatastreamsConfiguration> configurations = Arrays.asList(conf1, conf2, conf3);
        Map<String, Integer> testMapper1 = new HashMap<>();
        testMapper1.put(TEST_DEVICE_ID_1, TEST_SLAVE_ADDRESS_1);
        testMapper1.put(TEST_DEVICE_ID_2, TEST_SLAVE_ADDRESS_2);
        Map<String, Integer> testMapper3 = Collections.singletonMap(TEST_DEVICE_ID_3, TEST_SLAVE_ADDRESS_3);

        when(mockedFactory.createModbusDatastreamsGetter(anyString(), any(Type.class), any(), any(ModbusType.class),
                anyInt())).thenReturn(mockedDatastreamsGetter);
        when(mockedFactory.createModbusDatastreamsSetter(anyString(), any(Type.class), any(), any(ModbusType.class),
                anyInt())).thenReturn(mockedDatastreamsSetter);

        testModbusDatastreamsManager.loadConfiguration(configurations);

        verify(mockedRegistrationManager, times(2)).unregister();
        verify(mockedFactory).createModbusDatastreamsGetter(eq(TEST_DATASTREAM_ID_1), eq(TEST_DATASTREAM_TYPE_1),
                eq(testMapper1), eq(TEST_DATA_TYPE_1), eq(TEST_DATA_ADDRESS_1));
        verify(mockedFactory).createModbusDatastreamsGetter(eq(TEST_DATASTREAM_ID_3), eq(TEST_DATASTREAM_TYPE_3),
                eq(testMapper3), eq(TEST_DATA_TYPE_3), eq(TEST_DATA_ADDRESS_3));
        verify(mockedFactory).createModbusDatastreamsSetter(eq(TEST_DATASTREAM_ID_3), eq(TEST_DATASTREAM_TYPE_3),
                eq(testMapper3), eq(TEST_DATA_TYPE_3), eq(TEST_DATA_ADDRESS_3));
        verify(mockedRegistrationManager, times(2)).register(eq(mockedDatastreamsGetter));
        verify(mockedRegistrationManager).register(eq(mockedDatastreamsSetter));
    }

    @Test
    public void testClose() {
        testModbusDatastreamsManager.close();

        verify(mockedRegistrationManager, times(2)).unregister();
    }
}