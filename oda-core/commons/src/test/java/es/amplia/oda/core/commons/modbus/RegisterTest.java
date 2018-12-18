package es.amplia.oda.core.commons.modbus;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RegisterTest {

    private static final byte TEST_MOST_SIGNIFICANT_BYTE = (byte) 0x26;
    private static final byte TEST_LEAST_SIGNIFICANT_BYTE = (byte) 0x81;
    private static final byte[] TEST_BYTE_ARRAY = new byte[] {TEST_MOST_SIGNIFICANT_BYTE, TEST_LEAST_SIGNIFICANT_BYTE};
    private static final int TEST_VALUE = 9857;

    private final Register testRegister = new Register(TEST_MOST_SIGNIFICANT_BYTE, TEST_LEAST_SIGNIFICANT_BYTE);

    @Test
    public void testGetValue() {
        assertEquals(TEST_VALUE, testRegister.getValue());
    }

    @Test
    public void testToUnsignedShort() {
        assertEquals(TEST_VALUE, testRegister.toUnsignedShort());
    }

    @Test
    public void testToShort() {
        assertEquals(TEST_VALUE, testRegister.toShort());
    }

    @Test
    public void testToBytes() {
        assertArrayEquals(TEST_BYTE_ARRAY, testRegister.toBytes());
    }
}