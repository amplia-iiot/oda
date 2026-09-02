package es.amplia.oda.datastreams.modbusslave.translator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ModbusToJavaConvertorTest {

    private static final byte[] TEST_MODBUS_BYTES_SHORT = new byte[]{0, 15};
    private static final short TEST_MODBUS_BYTES_SHORT_CONVERTED = 15;

    private static final byte[] TEST_MODBUS_BYTES_INT = new byte[]{0, 0, 0, 25};
    private static final int TEST_MODBUS_BYTES_INT_CONVERTED = 25;

    private static final byte[] TEST_MODBUS_BYTES_LONG = new byte[]{0, 0, 0, 0, 7, 91, -51, 21};
    private static final long TEST_MODBUS_BYTES_LONG_CONVERTED = 123456789;

    private static final byte[] TEST_MODBUS_BYTES_FLOAT = new byte[]{66, -92, -82, 20};
    private static final float TEST_MODBUS_BYTES_FLOAT_CONVERTED = 82.34F;

    private static final byte[] TEST_MODBUS_BYTES_DOUBLE = new byte[]{64, 92, -23, 104, 114, -80, 32, -59};
    private static final double TEST_MODBUS_BYTES_DOUBLE_CONVERTED = 115.647;


    @Test
    public void testConvertRegisterToShort() {
        Object valueConverted;

        // check if byte array has wrong length for data type
        valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_INT, "Short");
        Assertions.assertNull(valueConverted);

        // call method to test
        valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_SHORT, "Short");

        // assertions
        Assertions.assertNotNull(valueConverted);
        Assertions.assertEquals(TEST_MODBUS_BYTES_SHORT_CONVERTED, valueConverted);
    }

   @Test
    public void testConvertRegisterToInt() {
       Object valueConverted;

       // check if byte array has wrong length for data type
       valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_SHORT, "Int");
       Assertions.assertNull(valueConverted);

        // call method to test
        valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_INT, "Int");

        // assertions
        Assertions.assertNotNull(valueConverted);
        Assertions.assertEquals(TEST_MODBUS_BYTES_INT_CONVERTED, valueConverted);
    }

    @Test
    public void testConvertRegisterToLong() {
        Object valueConverted;

        // check if byte array has wrong length for data type
        valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_SHORT, "Long");
        Assertions.assertNull(valueConverted);

        // call method to test
        valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_LONG, "Long");

        // assertions
        Assertions.assertNotNull(valueConverted);
        Assertions.assertEquals(TEST_MODBUS_BYTES_LONG_CONVERTED, valueConverted);
    }

    @Test
    public void testConvertRegisterToFloat() {
        Object valueConverted;

        // check if byte array has wrong length for data type
        valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_SHORT, "Float");
        Assertions.assertNull(valueConverted);

        // call method to test
        valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_FLOAT, "Float");

        // assertions
        Assertions.assertNotNull(valueConverted);
        Assertions.assertEquals(TEST_MODBUS_BYTES_FLOAT_CONVERTED, valueConverted);
    }

    @Test
    public void testConvertRegisterToDouble() {
        Object valueConverted;

        // check if byte array has wrong length for data type
        valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_SHORT, "Double");
        Assertions.assertNull(valueConverted);

        // call method to test
        valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_DOUBLE, "Double");

        // assertions
        Assertions.assertNotNull(valueConverted);
        Assertions.assertEquals(TEST_MODBUS_BYTES_DOUBLE_CONVERTED, valueConverted);
    }

    @Test
    public void testConvertRegisterNotSupported() {
        // call method to test
        Object valueConverted = ModbusToJavaTypeConverter.convertRegister(TEST_MODBUS_BYTES_DOUBLE, "NotSupported");

        // assertions
        Assertions.assertNull(valueConverted);
    }

    @Test
    public void testGetNumRegistersShort() {
        // call method to test
        int numRegister = ModbusToJavaTypeConverter.getNumRegisters("Short", 0);

        // assertions
        Assertions.assertEquals(1, numRegister);
    }

    @Test
    public void testGetNumRegistersInt() {
        // call method to test
        int numRegister = ModbusToJavaTypeConverter.getNumRegisters("Int", 0);

        // assertions
        Assertions.assertEquals(2, numRegister);
    }

    @Test
    public void testGetNumRegistersLong() {
        // call method to test
        int numRegister = ModbusToJavaTypeConverter.getNumRegisters("Long", 0);

        // assertions
        Assertions.assertEquals(4, numRegister);
    }

    @Test
    public void testGetNumRegistersFloat() {
        // call method to test
        int numRegister = ModbusToJavaTypeConverter.getNumRegisters("Float", 0);

        // assertions
        Assertions.assertEquals(2, numRegister);
    }

    @Test
    public void testGetNumRegistersDouble() {
        // call method to test
        int numRegister = ModbusToJavaTypeConverter.getNumRegisters("Double", 0);

        // assertions
        Assertions.assertEquals(4, numRegister);
    }

    @Test
    public void testGetNumRegistersList() {
        // call method to test
        int numRegister = ModbusToJavaTypeConverter.getNumRegisters("List",50);

        // assertions
        Assertions.assertEquals(50, numRegister);
    }

    @Test
    public void testGetNumRegistersNotSupported() {
        // call method to test
        int numRegister = ModbusToJavaTypeConverter.getNumRegisters("NotSupported",0);

        // assertions
        Assertions.assertEquals(0, numRegister);
    }
}
