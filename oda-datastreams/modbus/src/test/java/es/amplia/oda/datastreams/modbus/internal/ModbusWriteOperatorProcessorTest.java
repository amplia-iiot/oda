package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;
import es.amplia.oda.datastreams.modbus.ModbusType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModbusWriteOperatorProcessorTest {

    private static final int TEST_SLAVE_ADDRESS = 2;
    private static final int TEST_DATA_ADDRESS = 5;
    private static final boolean TEST_BOOLEAN_VALUE = true;
    private static final byte[] TEST_BYTE_ARRAY_VALUE = new byte[] { 0x1, 0x2 };
    private static final short TEST_SHORT_VALUE = (short) 1234;
    private static final int TEST_INTEGER_VALUE = 12345678;
    private static final float TEST_FLOAT_VALUE = 1234.5678f;
    private static final long TEST_LONG_VALUE = 1234567890123456L;
    private static final double TEST_DOUBLE_VALUE = 12345678.90123456;


    @Mock
    private ModbusMaster mockedModbusMaster;
    @Mock
    private JavaTypeToModbusTypeConverter mockedConverter;
    @InjectMocks
    private ModbusWriteOperatorProcessor testWriteOperatorProcessor;

    @Mock
    private Register mockedRegister;
    @Mock
    private Register mockedRegister2;
    @Mock
    private Register mockedRegister3;
    @Mock
    private Register mockedRegister4;

    private Register[] twoMockedRegisters;
    private Register[] fourMockedRegisters;

    @Before
    public void setUp() {
        twoMockedRegisters = new Register[] { mockedRegister, mockedRegister2 };
        fourMockedRegisters = new Register[] { mockedRegister, mockedRegister2, mockedRegister3, mockedRegister4 };
    }

    @Test
    public void testWriteBooleanToCoil() {
        testWriteOperatorProcessor
                .write(Boolean.class, ModbusType.COIL, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_BOOLEAN_VALUE);

        verify(mockedModbusMaster).writeCoil(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(TEST_BOOLEAN_VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteInvalidTypeToCoil() {
        testWriteOperatorProcessor
                .write(Integer.class, ModbusType.COIL, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_INTEGER_VALUE);
    }

    @Test
    public void testWriteByteArrayToHoldingRegister() {
        when(mockedConverter.convertByteArrayToRegister(any(byte[].class))).thenReturn(mockedRegister);

        testWriteOperatorProcessor.write(Byte[].class, ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, TEST_BYTE_ARRAY_VALUE);

        verify(mockedConverter).convertByteArrayToRegister(eq(TEST_BYTE_ARRAY_VALUE));
        verify(mockedModbusMaster).writeHoldingRegister(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(mockedRegister));
    }

    @Test
    public void testWriteShortToHoldingRegister() {
        when(mockedConverter.convertShortToRegister(anyShort())).thenReturn(mockedRegister);

        testWriteOperatorProcessor
                .write(Short.class, ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS, TEST_SHORT_VALUE);

        verify(mockedConverter).convertShortToRegister(eq(TEST_SHORT_VALUE));
        verify(mockedModbusMaster).writeHoldingRegister(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(mockedRegister));
    }

    @Test
    public void testWriteIntegerToHoldingRegister() {
        when(mockedConverter.convertIntegerToRegisters(anyInt())).thenReturn(twoMockedRegisters);

        testWriteOperatorProcessor.write(Integer.class, ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, TEST_INTEGER_VALUE);

        verify(mockedConverter).convertIntegerToRegisters(eq(TEST_INTEGER_VALUE));
        verify(mockedModbusMaster).writeHoldingRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(twoMockedRegisters));
    }

    @Test
    public void testWriteFloatToHoldingRegister() {
        when(mockedConverter.convertFloatToRegisters(anyFloat())).thenReturn(twoMockedRegisters);

        testWriteOperatorProcessor.write(Float.class, ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, TEST_FLOAT_VALUE);

        verify(mockedConverter).convertFloatToRegisters(eq(TEST_FLOAT_VALUE));
        verify(mockedModbusMaster).writeHoldingRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(twoMockedRegisters));
    }

    @Test
    public void testWriteLongToHoldingRegister() {
        when(mockedConverter.convertLongToRegisters(anyLong())).thenReturn(fourMockedRegisters);

        testWriteOperatorProcessor.write(Long.class, ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, TEST_LONG_VALUE);

        verify(mockedConverter).convertLongToRegisters(eq(TEST_LONG_VALUE));
        verify(mockedModbusMaster).writeHoldingRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(fourMockedRegisters));
    }

    @Test
    public void testWriteDoubleToHoldingRegister() {
        when(mockedConverter.convertDoubleToRegisters(anyDouble())).thenReturn(fourMockedRegisters);

        testWriteOperatorProcessor.write(Double.class, ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, TEST_DOUBLE_VALUE);

        verify(mockedConverter).convertDoubleToRegisters(eq(TEST_DOUBLE_VALUE));
        verify(mockedModbusMaster).writeHoldingRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(fourMockedRegisters));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteBooleanToHoldingRegister() {
        testWriteOperatorProcessor.write(Boolean.class, ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS, TEST_BOOLEAN_VALUE);
    }
}