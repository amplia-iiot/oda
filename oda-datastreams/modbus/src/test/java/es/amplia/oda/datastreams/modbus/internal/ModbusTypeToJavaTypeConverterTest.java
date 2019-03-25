package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.Register;

import org.junit.Test;

import static org.junit.Assert.*;

public class ModbusTypeToJavaTypeConverterTest {

    private final ModbusTypeToJavaTypeConverter testConverter = new ModbusTypeToJavaTypeConverter();

    @Test
    public void testConvertRegisterToByteArray() {
        byte[] testBytes = new byte[] { (byte) 0x12, (byte) 0x34 };
        Register testRegister = new Register(testBytes[0], testBytes[1]);

        byte[] result = testConverter.convertRegisterToByteArray(testRegister);

        assertArrayEquals(testBytes, result);
    }

    @Test
    public void testConvertRegisterToShort() {
        short testShort = (short) 1234;
        Register testRegister = new Register(testShort);

        short result = testConverter.convertRegisterToShort(testRegister);

        assertEquals(testShort, result);
    }

    @Test
    public void testConvertRegistersToInteger() {
        int testInteger = 12345678;
        Register[] testRegisters = new Register[] {
                new Register((testInteger >> 16) & 0x0000FFFF),
                new Register(testInteger & 0x0000FFFF)
        };

        int result = testConverter.convertRegistersToInteger(testRegisters);

        assertEquals(testInteger, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertRegistersToIntegerInvalidRegisterArraySize() {
        int testInteger = 12345678;
        Register[] testRegisters = new Register[] {
                new Register((testInteger >> 16) & 0x0000FFFF),
                new Register(testInteger & 0x0000FFFF),
                new Register(0),
                new Register(0)
        };

        testConverter.convertRegistersToInteger(testRegisters);
    }

    @Test
    public void testConvertRegistersToFloat() {
        float testFloat = 1234.5678f;
        int testFloatAsInt = Float.floatToIntBits(testFloat);
        Register[] testRegisters = new Register[] {
                new Register((testFloatAsInt >> 16) & 0x0000FFFF),
                new Register(testFloatAsInt & 0x0000FFFF)
        };

        float result = testConverter.convertRegistersToFloat(testRegisters);

        assertEquals(testFloat, result, 0.0001);
    }

    @Test
    public void testConvertRegistersToLong() {
        long testLong = 123456789012345678L;
        Register[] testRegisters = new Register[] {
                new Register((int) (testLong >> 48) & 0x0000FFFF),
                new Register((int) (testLong >> 32) & 0x0000FFFF),
                new Register((int) (testLong >> 16) & 0x0000FFFF),
                new Register((int) testLong & 0x0000FFFF)
        };

        long result = testConverter.convertRegistersToLong(testRegisters);

        assertEquals(testLong, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertRegistersToLongInvalidRegisterArraySize() {
        long testLong = 123456789012345678L;
        Register[] testRegisters = new Register[] {
                new Register((int) (testLong >> 16) & 0x0000FFFF),
                new Register((int) testLong & 0x0000FFFF)
        };

        testConverter.convertRegistersToLong(testRegisters);
    }

    @Test
    public void testConvertRegistersToDouble() {
        double testDouble = 12345678.90123456;
        long testDoubleAsLong = Double.doubleToLongBits(testDouble);
        Register[] testRegisters = new Register[] {
                new Register((int) (testDoubleAsLong >> 48) & 0x0000FFFF),
                new Register((int) (testDoubleAsLong >> 32) & 0x0000FFFF),
                new Register((int) (testDoubleAsLong >> 16) & 0x0000FFFF),
                new Register((int) testDoubleAsLong & 0x0000FFFF)
        };

        double result = testConverter.convertRegistersToDouble(testRegisters);

        assertEquals(testDouble, result, 0.00000001);
    }
}