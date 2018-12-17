package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;
import es.amplia.oda.datastreams.modbus.ModbusType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import static es.amplia.oda.datastreams.modbus.internal.ModbusReadOperatorProcessor.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModbusReadOperatorProcessorTest {

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
    private ModbusTypeToJavaTypeConverter mockedConverter;
    @InjectMocks
    private ModbusReadOperatorProcessor testReadOperatorProcessor;

    @Mock
    private Register mockedRegister;
    private Register[] dummyRegisters = new Register[0];

    @Test
    public void testReadBooleanFromInputDiscrete() {
        when(mockedModbusMaster.readInputDiscrete(anyInt(), anyInt())).thenReturn(TEST_BOOLEAN_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Boolean.class, ModbusType.INPUT_DISCRETE,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_BOOLEAN_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readInputDiscrete(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadInvalidTypeFromInputDiscrete() {
        testReadOperatorProcessor.read(Byte[].class, ModbusType.INPUT_DISCRETE, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);
    }

    @Test
    public void testReadBooleanFromCoil() {
        when(mockedModbusMaster.readCoil(anyInt(), anyInt())).thenReturn(TEST_BOOLEAN_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Boolean.class, ModbusType.COIL,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_BOOLEAN_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readCoil(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadInvalidTypeFromCoil() {
        testReadOperatorProcessor.read(Integer.class, ModbusType.COIL, TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);
    }

    @Test
    public void testReadByteArrayFromInputRegister() {
        when(mockedModbusMaster.readInputRegister(anyInt(), anyInt())).thenReturn(mockedRegister);
        when(mockedConverter.convertRegisterToByteArray(any(Register.class))).thenReturn(TEST_BYTE_ARRAY_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Byte[].class, ModbusType.INPUT_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertArrayEquals(TEST_BYTE_ARRAY_VALUE, (byte[]) collectedValue.getValue());
        verify(mockedModbusMaster).readInputRegister(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS));
        verify(mockedConverter).convertRegisterToByteArray(mockedRegister);
    }

    @Test
    public void testReadShortFromInputRegister() {
        when(mockedModbusMaster.readInputRegister(anyInt(), anyInt())).thenReturn(mockedRegister);
        when(mockedConverter.convertRegisterToShort(any(Register.class))).thenReturn(TEST_SHORT_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Short.class, ModbusType.INPUT_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_SHORT_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readInputRegister(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS));
        verify(mockedConverter).convertRegisterToShort(mockedRegister);
    }

    @Test
    public void testReadIntegerFromInputRegister() {
        when(mockedModbusMaster.readInputRegisters(anyInt(), anyInt(), anyInt())).thenReturn(dummyRegisters);
        when(mockedConverter.convertRegistersToInteger(any(Register[].class))).thenReturn(TEST_INTEGER_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Integer.class, ModbusType.INPUT_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_INTEGER_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readInputRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(TWO_REGISTERS));
        verify(mockedConverter).convertRegistersToInteger(eq(dummyRegisters));
    }

    @Test
    public void testReadFloatFromInputRegister() {
        when(mockedModbusMaster.readInputRegisters(anyInt(), anyInt(), anyInt())).thenReturn(dummyRegisters);
        when(mockedConverter.convertRegistersToFloat(any(Register[].class))).thenReturn(TEST_FLOAT_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Float.class, ModbusType.INPUT_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_FLOAT_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readInputRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(TWO_REGISTERS));
        verify(mockedConverter).convertRegistersToFloat(eq(dummyRegisters));
    }

    @Test
    public void testReadLongFromInputRegister() {
        when(mockedModbusMaster.readInputRegisters(anyInt(), anyInt(), anyInt())).thenReturn(dummyRegisters);
        when(mockedConverter.convertRegistersToLong(any(Register[].class))).thenReturn(TEST_LONG_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Long.class, ModbusType.INPUT_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_LONG_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readInputRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(FOUR_REGISTERS));
        verify(mockedConverter).convertRegistersToLong(eq(dummyRegisters));
    }

    @Test
    public void testReadDoubleFromInputRegister() {
        when(mockedModbusMaster.readInputRegisters(anyInt(), anyInt(), anyInt())).thenReturn(dummyRegisters);
        when(mockedConverter.convertRegistersToDouble(any(Register[].class))).thenReturn(TEST_DOUBLE_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Double.class, ModbusType.INPUT_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_DOUBLE_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readInputRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(FOUR_REGISTERS));
        verify(mockedConverter).convertRegistersToDouble(eq(dummyRegisters));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadInvalidDataTypeFromInputRegister() {
        testReadOperatorProcessor.read(Boolean.class, ModbusType.INPUT_REGISTER, TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS);
    }

    @Test
    public void testReadByteArrayFromHoldingRegister() {
        when(mockedModbusMaster.readHoldingRegister(anyInt(), anyInt())).thenReturn(mockedRegister);
        when(mockedConverter.convertRegisterToByteArray(any(Register.class))).thenReturn(TEST_BYTE_ARRAY_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Byte[].class, ModbusType.HOLDING_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertArrayEquals(TEST_BYTE_ARRAY_VALUE, (byte[]) collectedValue.getValue());
        verify(mockedModbusMaster).readHoldingRegister(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS));
        verify(mockedConverter).convertRegisterToByteArray(mockedRegister);
    }

    @Test
    public void testReadShortFromHoldingRegister() {
        when(mockedModbusMaster.readHoldingRegister(anyInt(), anyInt())).thenReturn(mockedRegister);
        when(mockedConverter.convertRegisterToShort(any(Register.class))).thenReturn(TEST_SHORT_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Short.class, ModbusType.HOLDING_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_SHORT_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readHoldingRegister(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS));
        verify(mockedConverter).convertRegisterToShort(mockedRegister);
    }

    @Test
    public void testReadIntegerFromHoldingRegister() {
        when(mockedModbusMaster.readHoldingRegisters(anyInt(), anyInt(), anyInt())).thenReturn(dummyRegisters);
        when(mockedConverter.convertRegistersToInteger(any(Register[].class))).thenReturn(TEST_INTEGER_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Integer.class, ModbusType.HOLDING_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_INTEGER_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readHoldingRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(TWO_REGISTERS));
        verify(mockedConverter).convertRegistersToInteger(eq(dummyRegisters));
    }

    @Test
    public void testReadFloatFromHoldingRegister() {
        when(mockedModbusMaster.readHoldingRegisters(anyInt(), anyInt(), anyInt())).thenReturn(dummyRegisters);
        when(mockedConverter.convertRegistersToFloat(any(Register[].class))).thenReturn(TEST_FLOAT_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Float.class, ModbusType.HOLDING_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_FLOAT_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readHoldingRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(TWO_REGISTERS));
        verify(mockedConverter).convertRegistersToFloat(eq(dummyRegisters));
    }

    @Test
    public void testReadLongFromHoldingRegister() {
        when(mockedModbusMaster.readHoldingRegisters(anyInt(), anyInt(), anyInt())).thenReturn(dummyRegisters);
        when(mockedConverter.convertRegistersToLong(any(Register[].class))).thenReturn(TEST_LONG_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Long.class, ModbusType.HOLDING_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_LONG_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readHoldingRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(FOUR_REGISTERS));
        verify(mockedConverter).convertRegistersToLong(eq(dummyRegisters));
    }

    @Test
    public void testReadDoubleFromHoldingRegister() {
        when(mockedModbusMaster.readHoldingRegisters(anyInt(), anyInt(), anyInt())).thenReturn(dummyRegisters);
        when(mockedConverter.convertRegistersToDouble(any(Register[].class))).thenReturn(TEST_DOUBLE_VALUE);

        CollectedValue collectedValue = testReadOperatorProcessor.read(Double.class, ModbusType.HOLDING_REGISTER,
                TEST_SLAVE_ADDRESS, TEST_DATA_ADDRESS);

        assertEquals(TEST_DOUBLE_VALUE, collectedValue.getValue());
        verify(mockedModbusMaster).readHoldingRegisters(eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(FOUR_REGISTERS));
        verify(mockedConverter).convertRegistersToDouble(eq(dummyRegisters));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadInvalidDataTypeFromHoldingRegister() {
        testReadOperatorProcessor.read(Boolean.class, ModbusType.HOLDING_REGISTER, TEST_SLAVE_ADDRESS,
                TEST_DATA_ADDRESS);
    }
}