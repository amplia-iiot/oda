package es.amplia.oda.datastreams.modbusslave.translator;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;
import es.amplia.oda.core.commons.utils.Event;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WriteCoilRequest.class, WriteMultipleCoilsRequest.class, WriteSingleRegisterRequest.class,
        WriteMultipleRegistersRequest.class, Register.class})
public class ModbusToEventConverterTest {

    private static final String TEST_DEVICE_ID = "deviceId";
    private static final String TEST_DATASTREAM_ID = "datastreamId";
    private static final String TEST_FEED_ID = "feedId";
    private static final String TEST_DATATYPE_BOOLEAN = "Boolean";
    private static final String TEST_DATATYPE_SHORT = "Short";
    private static final int TEST_MODBUS_ADDRESS = 254;
    private static final int TEST_MODBUS_SLAVE_ADDRESS = 1;

    @Mock
    WriteCoilRequest mockedModbusCoilRequest;
    @Mock
    WriteMultipleCoilsRequest mockedModbusCoilsRequest;
    @Mock
    WriteSingleRegisterRequest mockedModbusRegisterRequest;
    @Mock
    WriteMultipleRegistersRequest mockedModbusRegistersRequest;
    @Mock
    Register mockedRegister;

    @Test
    public void testTranslateCoil() {
        ModbusEventTranslator.clearAllEntries();

        // add translation
        ModbusEventTranslator.addEntry(new TranslationEntry(TEST_MODBUS_ADDRESS, TEST_DEVICE_ID,
                TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE_BOOLEAN));

        // create conditions for modbus request
        PowerMockito.when(mockedModbusCoilRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        PowerMockito.when(mockedModbusCoilRequest.getFunctionCode()).thenReturn(Modbus.WRITE_COIL);
        PowerMockito.when(mockedModbusCoilRequest.getMessage()).thenReturn(null);
        PowerMockito.when(mockedModbusCoilRequest.getReference()).thenReturn(TEST_MODBUS_ADDRESS);
        PowerMockito.when(mockedModbusCoilRequest.getCoil()).thenReturn(true);

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusCoilRequest);

        // assertions
        Assert.assertEquals(1, eventsGenerated.size());
        Assert.assertEquals(TEST_DEVICE_ID, eventsGenerated.get(0).getDeviceId());
        Assert.assertEquals(TEST_DATASTREAM_ID, eventsGenerated.get(0).getDatastreamId());
        Assert.assertEquals(TEST_FEED_ID, eventsGenerated.get(0).getFeed());
        Assert.assertEquals(true, eventsGenerated.get(0).getValue());
    }

    @Test
    public void testTranslateCoilNoTranslation() {
        ModbusEventTranslator.clearAllEntries();

        // create conditions for modbus request
        PowerMockito.when(mockedModbusCoilRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        PowerMockito.when(mockedModbusCoilRequest.getFunctionCode()).thenReturn(Modbus.WRITE_COIL);
        PowerMockito.when(mockedModbusCoilRequest.getMessage()).thenReturn(null);
        PowerMockito.when(mockedModbusCoilRequest.getReference()).thenReturn(TEST_MODBUS_ADDRESS);
        PowerMockito.when(mockedModbusCoilRequest.getCoil()).thenReturn(true);

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusCoilRequest);

        // assertions
        Assert.assertEquals(0, eventsGenerated.size());
    }

    @Test
    public void testTranslateCoils() {
        ModbusEventTranslator.clearAllEntries();

        // add translation
        ModbusEventTranslator.addEntry(new TranslationEntry(TEST_MODBUS_ADDRESS, TEST_DEVICE_ID,
                TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE_BOOLEAN));

        // create conditions for modbus request
        PowerMockito.when(mockedModbusCoilsRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        PowerMockito.when(mockedModbusCoilsRequest.getFunctionCode()).thenReturn(Modbus.WRITE_MULTIPLE_COILS);
        PowerMockito.when(mockedModbusCoilsRequest.getMessage()).thenReturn(null);
        PowerMockito.when(mockedModbusCoilsRequest.getReference()).thenReturn(TEST_MODBUS_ADDRESS);
        BitVector expectedValue = new BitVector(3);
        expectedValue.setBit(0, true);
        expectedValue.setBit(1, false);
        expectedValue.setBit(2, true);
        PowerMockito.when(mockedModbusCoilsRequest.getCoils()).thenReturn(expectedValue);

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusCoilsRequest);

        // assertions
        Assert.assertEquals(1, eventsGenerated.size());
        Assert.assertEquals(TEST_DEVICE_ID, eventsGenerated.get(0).getDeviceId());
        Assert.assertEquals(TEST_DATASTREAM_ID, eventsGenerated.get(0).getDatastreamId());
        Assert.assertEquals(TEST_FEED_ID, eventsGenerated.get(0).getFeed());
        Assert.assertEquals((byte) 5, eventsGenerated.get(0).getValue());
    }

    @Test
    public void testTranslateCoilsNoTranslation() {
        ModbusEventTranslator.clearAllEntries();

        // create conditions for modbus request
        PowerMockito.when(mockedModbusCoilsRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        PowerMockito.when(mockedModbusCoilsRequest.getFunctionCode()).thenReturn(Modbus.WRITE_MULTIPLE_COILS);
        PowerMockito.when(mockedModbusCoilsRequest.getMessage()).thenReturn(null);
        PowerMockito.when(mockedModbusCoilsRequest.getReference()).thenReturn(TEST_MODBUS_ADDRESS);
        BitVector expectedValue = new BitVector(3);
        expectedValue.setBit(0, true);
        expectedValue.setBit(1, false);
        expectedValue.setBit(2, true);
        PowerMockito.when(mockedModbusCoilsRequest.getCoils()).thenReturn(expectedValue);

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusCoilsRequest);

        // assertions
        Assert.assertEquals(0, eventsGenerated.size());
    }

    @Test
    public void testTranslateRegister() {
        ModbusEventTranslator.clearAllEntries();

        // add translation
        ModbusEventTranslator.addEntry(new TranslationEntry(TEST_MODBUS_ADDRESS, TEST_DEVICE_ID,
                TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE_SHORT));

        // create conditions for modbus request
        PowerMockito.when(mockedModbusRegisterRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        PowerMockito.when(mockedModbusRegisterRequest.getFunctionCode()).thenReturn(Modbus.WRITE_SINGLE_REGISTER);
        PowerMockito.when(mockedModbusRegisterRequest.getMessage()).thenReturn(null);
        PowerMockito.when(mockedModbusRegisterRequest.getReference()).thenReturn(TEST_MODBUS_ADDRESS);
        PowerMockito.when(mockedModbusRegisterRequest.getRegister()).thenReturn(mockedRegister);
        PowerMockito.when(mockedRegister.toBytes()).thenReturn(new byte[]{1, 0});

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusRegisterRequest);

        // assertions
        Assert.assertEquals(1, eventsGenerated.size());
        Assert.assertEquals(TEST_DEVICE_ID, eventsGenerated.get(0).getDeviceId());
        Assert.assertEquals(TEST_DATASTREAM_ID, eventsGenerated.get(0).getDatastreamId());
        Assert.assertEquals(TEST_FEED_ID, eventsGenerated.get(0).getFeed());
        Assert.assertEquals((short) 256, eventsGenerated.get(0).getValue());
    }

    @Test
    public void testTranslateRegisterNoTranslation() {
        ModbusEventTranslator.clearAllEntries();

        // create conditions for modbus request
        PowerMockito.when(mockedModbusRegisterRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        PowerMockito.when(mockedModbusRegisterRequest.getFunctionCode()).thenReturn(Modbus.WRITE_SINGLE_REGISTER);
        PowerMockito.when(mockedModbusRegisterRequest.getMessage()).thenReturn(null);
        PowerMockito.when(mockedModbusRegisterRequest.getReference()).thenReturn(TEST_MODBUS_ADDRESS);
        PowerMockito.when(mockedModbusRegisterRequest.getRegister()).thenReturn(mockedRegister);
        PowerMockito.when(mockedRegister.toBytes()).thenReturn(new byte[]{1, 0});

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusRegisterRequest);

        // assertions
        Assert.assertEquals(0, eventsGenerated.size());
    }

    @Test
    public void testTranslateRegisters() {
        ModbusEventTranslator.clearAllEntries();

        // add translation
        ModbusEventTranslator.addEntry(new TranslationEntry(TEST_MODBUS_ADDRESS, TEST_DEVICE_ID,
                TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE_SHORT));

        // create conditions for modbus request
        PowerMockito.when(mockedModbusRegistersRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        PowerMockito.when(mockedModbusRegistersRequest.getFunctionCode()).thenReturn(Modbus.WRITE_MULTIPLE_REGISTERS);
        PowerMockito.when(mockedModbusRegistersRequest.getMessage()).thenReturn(null);
        PowerMockito.when(mockedModbusRegistersRequest.getReference()).thenReturn(TEST_MODBUS_ADDRESS);
        PowerMockito.when(mockedModbusRegistersRequest.getWordCount()).thenReturn(1);
        // prepare value
        Register[] expectedValue = new Register[1];
        expectedValue[0] = mockedRegister;
        PowerMockito.when(mockedModbusRegistersRequest.getRegisters()).thenReturn(expectedValue);
        PowerMockito.when(mockedRegister.toBytes()).thenReturn(new byte[]{1, 0});

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusRegistersRequest);

        // assertions
        Assert.assertEquals(1, eventsGenerated.size());
        Assert.assertEquals(TEST_DEVICE_ID, eventsGenerated.get(0).getDeviceId());
        Assert.assertEquals(TEST_DATASTREAM_ID, eventsGenerated.get(0).getDatastreamId());
        Assert.assertEquals(TEST_FEED_ID, eventsGenerated.get(0).getFeed());
        Assert.assertEquals((short) 256, eventsGenerated.get(0).getValue());
    }

    @Test
    public void testTranslateRegistersNoTranslation() {
        ModbusEventTranslator.clearAllEntries();

        // create conditions for modbus request
        PowerMockito.when(mockedModbusRegistersRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        PowerMockito.when(mockedModbusRegistersRequest.getFunctionCode()).thenReturn(Modbus.WRITE_MULTIPLE_REGISTERS);
        PowerMockito.when(mockedModbusRegistersRequest.getMessage()).thenReturn(null);
        PowerMockito.when(mockedModbusRegistersRequest.getReference()).thenReturn(TEST_MODBUS_ADDRESS);
        PowerMockito.when(mockedModbusRegistersRequest.getWordCount()).thenReturn(1);
        // prepare value
        Register[] expectedValue = new Register[1];
        expectedValue[0] = mockedRegister;
        PowerMockito.when(mockedModbusRegistersRequest.getRegisters()).thenReturn(expectedValue);
        PowerMockito.when(mockedRegister.toBytes()).thenReturn(new byte[]{1, 0});

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusRegistersRequest);

        // assertions
        Assert.assertEquals(0, eventsGenerated.size());
    }
}
