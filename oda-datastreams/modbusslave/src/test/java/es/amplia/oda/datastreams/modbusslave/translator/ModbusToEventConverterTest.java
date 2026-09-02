package es.amplia.oda.datastreams.modbusslave.translator;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;
import es.amplia.oda.core.commons.utils.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ModbusToEventConverterTest {

    private static final String TEST_DEVICE_ID = "deviceId";
    private static final String TEST_DATASTREAM_ID = "datastreamId";
    private static final String TEST_FEED_ID = "feedId";
    private static final String TEST_DATATYPE_BOOLEAN = "Boolean";
    private static final String TEST_DATATYPE_SHORT = "Short";
    private static final int TEST_START_MODBUS_ADDRESS = 254;
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
        ModbusEventTranslator.addEntry(new TranslationEntry(TEST_START_MODBUS_ADDRESS, TEST_START_MODBUS_ADDRESS,
                TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE_BOOLEAN));

        // create conditions for modbus request
        Mockito.when(mockedModbusCoilRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        Mockito.when(mockedModbusCoilRequest.getFunctionCode()).thenReturn(Modbus.WRITE_COIL);
        Mockito.when(mockedModbusCoilRequest.getMessage()).thenReturn(null);
        Mockito.when(mockedModbusCoilRequest.getReference()).thenReturn(TEST_START_MODBUS_ADDRESS);
        Mockito.when(mockedModbusCoilRequest.getCoil()).thenReturn(true);

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusCoilRequest);

        // assertions
        Assertions.assertEquals(1, eventsGenerated.size());
        Assertions.assertEquals(TEST_DEVICE_ID, eventsGenerated.get(0).getDeviceId());
        Assertions.assertEquals(TEST_DATASTREAM_ID, eventsGenerated.get(0).getDatastreamId());
        Assertions.assertEquals(TEST_FEED_ID, eventsGenerated.get(0).getFeed());
        Assertions.assertEquals(true, eventsGenerated.get(0).getValue());
    }

    @Test
    public void testTranslateCoilNoTranslation() {
        ModbusEventTranslator.clearAllEntries();

        // create conditions for modbus request
        Mockito.when(mockedModbusCoilRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        Mockito.when(mockedModbusCoilRequest.getFunctionCode()).thenReturn(Modbus.WRITE_COIL);
        Mockito.when(mockedModbusCoilRequest.getMessage()).thenReturn(null);
        Mockito.when(mockedModbusCoilRequest.getReference()).thenReturn(TEST_START_MODBUS_ADDRESS);
        Mockito.when(mockedModbusCoilRequest.getCoil()).thenReturn(true);

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusCoilRequest);

        // assertions
        Assertions.assertEquals(0, eventsGenerated.size());
    }

    @Test
    public void testTranslateCoils() {
        ModbusEventTranslator.clearAllEntries();

        // add translation
        ModbusEventTranslator.addEntry(new TranslationEntry(TEST_START_MODBUS_ADDRESS, TEST_START_MODBUS_ADDRESS,
                TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE_BOOLEAN));

        // create conditions for modbus request
        Mockito.when(mockedModbusCoilsRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        Mockito.when(mockedModbusCoilsRequest.getFunctionCode()).thenReturn(Modbus.WRITE_MULTIPLE_COILS);
        Mockito.when(mockedModbusCoilsRequest.getMessage()).thenReturn(null);
        Mockito.when(mockedModbusCoilsRequest.getReference()).thenReturn(TEST_START_MODBUS_ADDRESS);
        BitVector expectedValue = new BitVector(3);
        expectedValue.setBit(0, true);
        expectedValue.setBit(1, false);
        expectedValue.setBit(2, true);
        Mockito.when(mockedModbusCoilsRequest.getCoils()).thenReturn(expectedValue);

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusCoilsRequest);

        // assertions
        Assertions.assertEquals(1, eventsGenerated.size());
        Assertions.assertEquals(TEST_DEVICE_ID, eventsGenerated.get(0).getDeviceId());
        Assertions.assertEquals(TEST_DATASTREAM_ID, eventsGenerated.get(0).getDatastreamId());
        Assertions.assertEquals(TEST_FEED_ID, eventsGenerated.get(0).getFeed());
        Assertions.assertEquals(true, eventsGenerated.get(0).getValue());
    }

    @Test
    public void testTranslateCoilsNoTranslation() {
        ModbusEventTranslator.clearAllEntries();

        // create conditions for modbus request
        Mockito.when(mockedModbusCoilsRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        Mockito.when(mockedModbusCoilsRequest.getFunctionCode()).thenReturn(Modbus.WRITE_MULTIPLE_COILS);
        Mockito.when(mockedModbusCoilsRequest.getMessage()).thenReturn(null);
        Mockito.when(mockedModbusCoilsRequest.getReference()).thenReturn(TEST_START_MODBUS_ADDRESS);
        BitVector expectedValue = new BitVector(3);
        expectedValue.setBit(0, true);
        expectedValue.setBit(1, false);
        expectedValue.setBit(2, true);
        Mockito.when(mockedModbusCoilsRequest.getCoils()).thenReturn(expectedValue);

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusCoilsRequest);

        // assertions
        Assertions.assertEquals(0, eventsGenerated.size());
    }

    @Test
    public void testTranslateRegister() {
        ModbusEventTranslator.clearAllEntries();

        // add translation
        ModbusEventTranslator.addEntry(new TranslationEntry(TEST_START_MODBUS_ADDRESS, TEST_START_MODBUS_ADDRESS,
                TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE_SHORT));

        // create conditions for modbus request
        Mockito.when(mockedModbusRegisterRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        Mockito.when(mockedModbusRegisterRequest.getFunctionCode()).thenReturn(Modbus.WRITE_SINGLE_REGISTER);
        Mockito.when(mockedModbusRegisterRequest.getMessage()).thenReturn(null);
        Mockito.when(mockedModbusRegisterRequest.getReference()).thenReturn(TEST_START_MODBUS_ADDRESS);
        Mockito.when(mockedModbusRegisterRequest.getRegister()).thenReturn(mockedRegister);
        Mockito.when(mockedRegister.toBytes()).thenReturn(new byte[]{1, 0});

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusRegisterRequest);

        // assertions
        Assertions.assertEquals(1, eventsGenerated.size());
        Assertions.assertEquals(TEST_DEVICE_ID, eventsGenerated.get(0).getDeviceId());
        Assertions.assertEquals(TEST_DATASTREAM_ID, eventsGenerated.get(0).getDatastreamId());
        Assertions.assertEquals(TEST_FEED_ID, eventsGenerated.get(0).getFeed());
        Assertions.assertEquals((short) 256, eventsGenerated.get(0).getValue());
    }

    @Test
    public void testTranslateRegisterNoTranslation() {
        ModbusEventTranslator.clearAllEntries();

        // create conditions for modbus request
        Mockito.when(mockedModbusRegisterRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        Mockito.when(mockedModbusRegisterRequest.getFunctionCode()).thenReturn(Modbus.WRITE_SINGLE_REGISTER);
        Mockito.when(mockedModbusRegisterRequest.getMessage()).thenReturn(null);
        Mockito.when(mockedModbusRegisterRequest.getReference()).thenReturn(TEST_START_MODBUS_ADDRESS);
        Mockito.when(mockedModbusRegisterRequest.getRegister()).thenReturn(mockedRegister);
        Mockito.when(mockedRegister.toBytes()).thenReturn(new byte[]{1, 0});

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusRegisterRequest);

        // assertions
        Assertions.assertEquals(0, eventsGenerated.size());
    }

    @Test
    public void testTranslateRegisters() {
        ModbusEventTranslator.clearAllEntries();

        // add translation
        ModbusEventTranslator.addEntry(new TranslationEntry(TEST_START_MODBUS_ADDRESS, TEST_START_MODBUS_ADDRESS,
                TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED_ID, TEST_DATATYPE_SHORT));

        // create conditions for modbus request
        Mockito.when(mockedModbusRegistersRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        Mockito.when(mockedModbusRegistersRequest.getFunctionCode()).thenReturn(Modbus.WRITE_MULTIPLE_REGISTERS);
        Mockito.when(mockedModbusRegistersRequest.getMessage()).thenReturn(null);
        Mockito.when(mockedModbusRegistersRequest.getReference()).thenReturn(TEST_START_MODBUS_ADDRESS);
        Mockito.when(mockedModbusRegistersRequest.getWordCount()).thenReturn(1);
        // prepare value
        Register[] expectedValue = new Register[1];
        expectedValue[0] = mockedRegister;
        Mockito.when(mockedModbusRegistersRequest.getRegisters()).thenReturn(expectedValue);
        Mockito.when(mockedRegister.toBytes()).thenReturn(new byte[]{1, 0});

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusRegistersRequest);

        // assertions
        Assertions.assertEquals(1, eventsGenerated.size());
        Assertions.assertEquals(TEST_DEVICE_ID, eventsGenerated.get(0).getDeviceId());
        Assertions.assertEquals(TEST_DATASTREAM_ID, eventsGenerated.get(0).getDatastreamId());
        Assertions.assertEquals(TEST_FEED_ID, eventsGenerated.get(0).getFeed());
        Assertions.assertEquals((short) 256, eventsGenerated.get(0).getValue());
    }

    @Test
    public void testTranslateRegistersNoTranslation() {
        ModbusEventTranslator.clearAllEntries();

        // create conditions for modbus request
        Mockito.when(mockedModbusRegistersRequest.getUnitID()).thenReturn(TEST_MODBUS_SLAVE_ADDRESS);
        Mockito.when(mockedModbusRegistersRequest.getFunctionCode()).thenReturn(Modbus.WRITE_MULTIPLE_REGISTERS);
        Mockito.when(mockedModbusRegistersRequest.getMessage()).thenReturn(null);
        Mockito.when(mockedModbusRegistersRequest.getReference()).thenReturn(TEST_START_MODBUS_ADDRESS);
        Mockito.when(mockedModbusRegistersRequest.getWordCount()).thenReturn(1);
        // prepare value
        Register[] expectedValue = new Register[1];
        expectedValue[0] = mockedRegister;
        Mockito.when(mockedModbusRegistersRequest.getRegisters()).thenReturn(expectedValue);
        Mockito.when(mockedRegister.toBytes()).thenReturn(new byte[]{1, 0});

        // call method to test
        List<Event> eventsGenerated = ModbusToEventConverter.translateEvent(TEST_DEVICE_ID, mockedModbusRegistersRequest);

        // assertions
        Assertions.assertEquals(0, eventsGenerated.size());
    }
}
