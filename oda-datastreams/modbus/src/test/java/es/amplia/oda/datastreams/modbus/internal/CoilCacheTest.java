package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.datastreams.modbus.ModbusConnectionsFinder;
import es.amplia.oda.datastreams.modbus.ModbusType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import java.util.List;

import static es.amplia.oda.datastreams.modbus.internal.ModbusReadOperatorProcessor.ONE_REGISTER;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CoilCacheTest {

    private static final int NUM_REGISTERS_TO_READ = 4;
    private static final int TEST_SLAVE_ADDRESS = 2;
    private static final int TEST_BLOCK_DATA_ADDRESS = 0;
    private static final int TEST_DATA_ADDRESS = 1;
    private static final boolean TEST_BOOLEAN_VALUE = true;
    private static final String TEST_DEVICE_ID = "TestDeviceId";
    private static final String TEST_DEVICE_MANUFACTURER = "TestManufacturer";
    private static final boolean TEST_READ_FROM_CACHE_TRUE = true;
    private static final boolean TEST_READ_FROM_CACHE_FALSE = true;
    private static final Boolean[] TEST_BOOLEAN_ARRAY = new Boolean[NUM_REGISTERS_TO_READ];


    @Mock
    private ModbusConnectionsFinder mockedConnectionsLocator;
    @Mock
    private ModbusMaster mockedModbusMaster;
    @InjectMocks
    private ModbusReadOperatorProcessor testReadOperatorProcessor;


    @Before
    public void setUp() {
        // ini boolean array
        TEST_BOOLEAN_ARRAY[0] = false;
        TEST_BOOLEAN_ARRAY[1] = true;
        TEST_BOOLEAN_ARRAY[2] = true;
        TEST_BOOLEAN_ARRAY[3] = false;

        PowerMockito.when(mockedConnectionsLocator.getModbusConnectionWithId(anyString())).thenReturn(mockedModbusMaster);
        PowerMockito.when(mockedModbusMaster.getDeviceManufacturer()).thenReturn(TEST_DEVICE_MANUFACTURER);
        PowerMockito.when(mockedModbusMaster.readCoils(anyInt(), anyInt(), anyInt())).thenReturn(TEST_BOOLEAN_ARRAY);

        // read block and save data in cache
        testReadOperatorProcessor.read(TEST_DEVICE_ID, List.class, ModbusType.COIL, TEST_SLAVE_ADDRESS,
                TEST_BLOCK_DATA_ADDRESS, TEST_READ_FROM_CACHE_FALSE, NUM_REGISTERS_TO_READ);
    }

    @Test
    public void testReadBooleanFromCoilFromCache() throws InterruptedException {

        // read data from cache
        DatastreamsGetter.CollectedValue collectedValue = testReadOperatorProcessor.read(TEST_DEVICE_ID, Boolean.class,
                ModbusType.COIL, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_READ_FROM_CACHE_TRUE, ONE_REGISTER);

        // wait 100 ms to check that the date from value retrieved is the one from the cache
        sleep(100);

        assertEquals(TEST_BOOLEAN_VALUE, collectedValue.getValue());
        // check that the time retrieved is not the current time
        assertTrue(System.currentTimeMillis() > collectedValue.getAt());
        // check that there has been a petition of the block
        verify(mockedModbusMaster, times(1)).readCoils(TEST_SLAVE_ADDRESS,
                TEST_BLOCK_DATA_ADDRESS, NUM_REGISTERS_TO_READ);
        // check that there hasn't been a petition of the single data
        verify(mockedModbusMaster, times(0)).readCoils(TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, ONE_REGISTER);
    }
}
