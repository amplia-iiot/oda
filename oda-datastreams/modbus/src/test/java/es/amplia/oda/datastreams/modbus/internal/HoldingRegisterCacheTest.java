package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;
import es.amplia.oda.datastreams.modbus.ModbusConnectionsFinder;
import es.amplia.oda.hardware.modbus.ModbusType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import java.util.Collections;
import java.util.List;

import static es.amplia.oda.datastreams.modbus.internal.ModbusReadOperatorProcessor.*;
import static java.lang.Thread.sleep;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HoldingRegisterCacheTest {

    private static final int NUM_REGISTERS_TO_READ = 5;
    private static final int TEST_SLAVE_ADDRESS = 2;
    private static final int TEST_BLOCK_DATA_ADDRESS = 0;
    private static final int TEST_DATA_ADDRESS = 1;
    private static final String TEST_DEVICE_ID = "TestDeviceId";
    private static final String TEST_DEVICE_MANUFACTURER = "TestManufacturer";
    private static final boolean TEST_READ_FROM_CACHE_TRUE = true;
    private static final boolean TEST_READ_FROM_CACHE_FALSE = true;
    private static final Register[] TEST_HOLDING_REGISTER_ARRAY = new Register[NUM_REGISTERS_TO_READ];
    private static final byte[] TEST_BYTE_ARRAY_VALUE = new byte[] { 0x0, 0x1 };
    private static final short TEST_SHORT_VALUE = 1;
    private static final int TEST_INTEGER_VALUE = 65538;
    private static final float TEST_FLOAT_VALUE = 9.1838E-41F;
    private static final long TEST_LONG_VALUE = 281483566841860L;
    private static final double TEST_DOUBLE_VALUE = 1.390713602454213E-309;

    @Mock
    private ModbusConnectionsFinder mockedConnectionsLocator;
    @Mock
    private ModbusMaster mockedModbusMaster;
    private ModbusReadOperatorProcessor testReadOperatorProcessor;


    @Before
    public void setUp() throws InterruptedException {
        // ini boolean array
        TEST_HOLDING_REGISTER_ARRAY[0] = new Register(0);
        TEST_HOLDING_REGISTER_ARRAY[1] = new Register(1);
        TEST_HOLDING_REGISTER_ARRAY[2] = new Register(2);
        TEST_HOLDING_REGISTER_ARRAY[3] = new Register(3);
        TEST_HOLDING_REGISTER_ARRAY[4] = new Register(4);

        List<ModbusMaster> listModbusConnections = Collections.singletonList(mockedModbusMaster);
        PowerMockito.when(mockedConnectionsLocator.getAllModbusConnections()).thenReturn(listModbusConnections);
        PowerMockito.when(mockedConnectionsLocator.getModbusConnectionWithId(anyString())).thenReturn(mockedModbusMaster);
        PowerMockito.when(mockedModbusMaster.getDeviceManufacturer()).thenReturn(TEST_DEVICE_MANUFACTURER);
        PowerMockito.when(mockedModbusMaster.readHoldingRegisters(anyInt(), anyInt(), anyInt())).thenReturn(TEST_HOLDING_REGISTER_ARRAY);

        testReadOperatorProcessor = new ModbusReadOperatorProcessor(mockedConnectionsLocator, new ModbusTypeToJavaTypeConverter());

        // read block and save data in cache
        testReadOperatorProcessor.read(TEST_DEVICE_ID, List.class, ModbusType.HOLDING_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_BLOCK_DATA_ADDRESS, TEST_READ_FROM_CACHE_FALSE, NUM_REGISTERS_TO_READ);

        // wait 100 ms to check that the date from value retrieved is the one from the cache
        sleep(100);
    }

    @Test
    public void testReadBytesFromHoldingRegisterFromCache() {

        // read data from cache
        DatastreamsGetter.CollectedValue collectedValue = testReadOperatorProcessor.read(TEST_DEVICE_ID, Byte[].class,
                ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_READ_FROM_CACHE_TRUE, ONE_REGISTER);

        assertArrayEquals(TEST_BYTE_ARRAY_VALUE, (byte[]) collectedValue.getValue());
        // check that the time retrieved is not the current time
        assertTrue(System.currentTimeMillis() > collectedValue.getAt());
        // check that there has been a petition of the block
        verify(mockedModbusMaster, times(1)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_BLOCK_DATA_ADDRESS, NUM_REGISTERS_TO_READ);
        // check that there hasn't been a petition of the single data
        verify(mockedModbusMaster, times(0)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, ONE_REGISTER);
    }

    @Test
    public void testReadShortFromHoldingRegisterFromCache() {

        // read data from cache
        DatastreamsGetter.CollectedValue collectedValue = testReadOperatorProcessor.read(TEST_DEVICE_ID, Short.class,
                ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_READ_FROM_CACHE_TRUE, ONE_REGISTER);

        assertEquals(TEST_SHORT_VALUE, collectedValue.getValue());
        // check that the time retrieved is not the current time
        assertTrue(System.currentTimeMillis() > collectedValue.getAt());
        // check that there has been a petition of the block
        verify(mockedModbusMaster, times(1)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_BLOCK_DATA_ADDRESS, NUM_REGISTERS_TO_READ);
        // check that there hasn't been a petition of the single data
        verify(mockedModbusMaster, times(0)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, ONE_REGISTER);
    }

    @Test
    public void testReadIntegerFromHoldingRegisterFromCache() {

        // read data from cache
        DatastreamsGetter.CollectedValue collectedValue = testReadOperatorProcessor.read(TEST_DEVICE_ID, Integer.class,
                ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_READ_FROM_CACHE_TRUE, TWO_REGISTERS);

        assertEquals(TEST_INTEGER_VALUE, collectedValue.getValue());
        // check that the time retrieved is not the current time
        assertTrue(System.currentTimeMillis() > collectedValue.getAt());
        // check that there has been a petition of the block
        verify(mockedModbusMaster, times(1)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_BLOCK_DATA_ADDRESS, NUM_REGISTERS_TO_READ);
        // check that there hasn't been a petition of the single data
        verify(mockedModbusMaster, times(0)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, ONE_REGISTER);
    }

    @Test
    public void testReadFloatFromHoldingRegisterFromCache() {

        // read data from cache
        DatastreamsGetter.CollectedValue collectedValue = testReadOperatorProcessor.read(TEST_DEVICE_ID, Float.class,
                ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_READ_FROM_CACHE_TRUE, TWO_REGISTERS);

        assertEquals(TEST_FLOAT_VALUE, collectedValue.getValue());
        // check that the time retrieved is not the current time
        assertTrue(System.currentTimeMillis() > collectedValue.getAt());
        // check that there has been a petition of the block
        verify(mockedModbusMaster, times(1)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_BLOCK_DATA_ADDRESS, NUM_REGISTERS_TO_READ);
        // check that there hasn't been a petition of the single data
        verify(mockedModbusMaster, times(0)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, ONE_REGISTER);
    }

    @Test
    public void testReadLongFromHoldingRegisterFromCache() {

        // read data from cache
        DatastreamsGetter.CollectedValue collectedValue = testReadOperatorProcessor.read(TEST_DEVICE_ID, Long.class,
                ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_READ_FROM_CACHE_TRUE, FOUR_REGISTERS);

        assertEquals(TEST_LONG_VALUE, collectedValue.getValue());
        // check that the time retrieved is not the current time
        assertTrue(System.currentTimeMillis() > collectedValue.getAt());
        // check that there has been a petition of the block
        verify(mockedModbusMaster, times(1)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_BLOCK_DATA_ADDRESS, NUM_REGISTERS_TO_READ);
        // check that there hasn't been a petition of the single data
        verify(mockedModbusMaster, times(0)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, ONE_REGISTER);
    }

    @Test
    public void testReadDoubleFromHoldingRegisterFromCache() {

        // read data from cache
        DatastreamsGetter.CollectedValue collectedValue = testReadOperatorProcessor.read(TEST_DEVICE_ID, Double.class,
                ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_READ_FROM_CACHE_TRUE, FOUR_REGISTERS);

        assertEquals(TEST_DOUBLE_VALUE, collectedValue.getValue());
        // check that the time retrieved is not the current time
        assertTrue(System.currentTimeMillis() > collectedValue.getAt());
        // check that there has been a petition of the block
        verify(mockedModbusMaster, times(1)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_BLOCK_DATA_ADDRESS, NUM_REGISTERS_TO_READ);
        // check that there hasn't been a petition of the single data
        verify(mockedModbusMaster, times(0)).readHoldingRegisters(TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, ONE_REGISTER);
    }
}
