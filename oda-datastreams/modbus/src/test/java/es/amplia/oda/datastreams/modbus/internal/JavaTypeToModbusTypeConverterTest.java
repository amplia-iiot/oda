package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.Register;

import org.junit.Test;

import static es.amplia.oda.datastreams.modbus.internal.JavaTypeToModbusTypeConverter.*;

import static org.junit.Assert.*;

public class JavaTypeToModbusTypeConverterTest {

    private final JavaTypeToModbusTypeConverter testConverter = new JavaTypeToModbusTypeConverter();

    @Test
    public void testConvertByteArrayToRegister() {
        byte[] testByteArray = new byte[] { (byte) 0x01, (byte) 0x02};

        Register result = testConverter.convertByteArrayToRegister(testByteArray);

        assertArrayEquals(testByteArray, result.toBytes());
    }

    @Test
    public void testConvertShortToRegister() {
        short testShort = (short) 1234;

        Register register = testConverter.convertShortToRegister(testShort);

        assertEquals(testShort, register.toShort());
    }

    @Test
    public void testConvertIntegerToRegisters() {
        int testInteger = 123456;

        Register[] registers = testConverter.convertIntegerToRegisters(testInteger);

        assertEquals(TWO_REGISTERS, registers.length);
        assertEquals((testInteger >> 16) & 0x0000FFFF, registers[0].getValue());
        assertEquals(testInteger & 0x0000FFFF, registers[1].getValue());
    }

    @Test
    public void testConvertFloatToRegisters() {
        float testFloat = 123.456f;

        Register[] registers = testConverter.convertFloatToRegisters(testFloat);

        assertEquals(TWO_REGISTERS, registers.length);
        int testFloatAsInt = Float.floatToIntBits(testFloat);
        assertEquals((testFloatAsInt >> 16) & 0x0000FFFF, registers[0].getValue());
        assertEquals(testFloatAsInt & 0x0000FFFF, registers[1].getValue());
    }

    @Test
    public void testConvertLongToRegisters() {
        long testLong = 1234567890123456L;

        Register[] registers = testConverter.convertLongToRegisters(testLong);

        assertEquals(FOUR_REGISTERS, registers.length);
        assertEquals((testLong >> 48) & 0x0000FFFF, registers[0].getValue());
        assertEquals((testLong >> 32) & 0x0000FFFF, registers[1].getValue());
        assertEquals((testLong >> 16) & 0x0000FFFF, registers[2].getValue());
        assertEquals(testLong & 0x0000FFFF, registers[3].getValue());
    }

    @Test
    public void testConvertDoubleToRegisters() {
        double testDouble = 12345678.90123456;

        Register[] registers = testConverter.convertDoubleToRegisters(testDouble);

        assertEquals(FOUR_REGISTERS, registers.length);
        long testDoubleAsLong = Double.doubleToLongBits(testDouble);
        assertEquals((testDoubleAsLong >> 48) & 0x0000FFFF, registers[0].getValue());
        assertEquals((testDoubleAsLong >> 32) & 0x0000FFFF, registers[1].getValue());
        assertEquals((testDoubleAsLong >> 16) & 0x0000FFFF, registers[2].getValue());
        assertEquals(testDoubleAsLong & 0x0000FFFF, registers[3].getValue());
    }
}