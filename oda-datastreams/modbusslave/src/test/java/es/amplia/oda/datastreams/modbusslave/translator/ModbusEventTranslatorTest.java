package es.amplia.oda.datastreams.modbusslave.translator;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ModbusEventTranslatorTest {

    private static final String TEST_DEVICE_ID = "deviceId";
    private static final String TEST_DATASTREAM_ID = "datastreamId";
    private static final String TEST_FEED_ID = "feedId";
    private static final String TEST_DATATYPE = "Short";
    private static final int TEST_START_MODBUS_ADDRESS = 254;
    private static final int TEST_END_MODBUS_ADDRESS = 286;

    @Test
    public void testAddEntry() {

        // create new translation entry
        TranslationEntry expectedTranslationEntry = new TranslationEntry(TEST_START_MODBUS_ADDRESS, TEST_START_MODBUS_ADDRESS,
                TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE);

        // call load method to test
        ModbusEventTranslator.addEntry(expectedTranslationEntry);

        // retrieve loaded entries
        List<TranslationEntry> currentTranslationEntries = ModbusEventTranslator.getExistingNonBlockTranslations(TEST_START_MODBUS_ADDRESS, TEST_DEVICE_ID);

        // assertions
        Assert.assertEquals(1, currentTranslationEntries.size());
        Assert.assertEquals(expectedTranslationEntry, currentTranslationEntries.get(0));
    }

    @Test
    public void testGetTranslation() {

        // create new translation entry
        TranslationEntry expectedTranslationEntry = new TranslationEntry(TEST_START_MODBUS_ADDRESS, TEST_START_MODBUS_ADDRESS,
                TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE);

        // call load method
        ModbusEventTranslator.addEntry(expectedTranslationEntry);

        // call load method to test
        List<TranslationEntry> currentTranslationEntry = ModbusEventTranslator.getExistingNonBlockTranslations(TEST_START_MODBUS_ADDRESS, TEST_DEVICE_ID);

        // assertions
        Assert.assertEquals(expectedTranslationEntry, currentTranslationEntry.get(0));
    }

    @Test
    public void testClearAllTranslations() {
        ModbusEventTranslator.clearAllEntries();

        // retrieve loaded entries before
        List<TranslationEntry> beforeTranslationEntries = ModbusEventTranslator.getExistingNonBlockTranslations(TEST_START_MODBUS_ADDRESS, TEST_DEVICE_ID);
        // assertions
        Assert.assertEquals(0, beforeTranslationEntries.size());

        // create new translation entry
        TranslationEntry expectedTranslationEntry = new TranslationEntry(TEST_START_MODBUS_ADDRESS, TEST_START_MODBUS_ADDRESS,
                TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE);

        // call load method
        ModbusEventTranslator.addEntry(expectedTranslationEntry);

        // retrieve loaded entries
        List<TranslationEntry> currentTranslationEntries = ModbusEventTranslator.getExistingNonBlockTranslations(TEST_START_MODBUS_ADDRESS, TEST_DEVICE_ID);

        // assertions
        Assert.assertEquals(1, currentTranslationEntries.size());

        // call method to test
        ModbusEventTranslator.clearAllEntries();

        // retrieve loaded entries
        List<TranslationEntry> afterTranslationEntries = ModbusEventTranslator.getExistingNonBlockTranslations(TEST_START_MODBUS_ADDRESS, TEST_DEVICE_ID);

        // assertions
        Assert.assertEquals(0, afterTranslationEntries.size());
    }

    @Test
    public void testGetTranslationNotExist() {
        // clear all entries
        ModbusEventTranslator.clearAllEntries();

        // call load method to test
        List<TranslationEntry> currentTranslationEntry = ModbusEventTranslator.getExistingNonBlockTranslations(TEST_START_MODBUS_ADDRESS, TEST_DEVICE_ID);

        // assertions
        Assert.assertEquals(0, currentTranslationEntry.size());
    }
}
