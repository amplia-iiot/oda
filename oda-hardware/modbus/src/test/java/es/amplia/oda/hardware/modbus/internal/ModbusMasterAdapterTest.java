package es.amplia.oda.hardware.modbus.internal;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import es.amplia.oda.core.commons.modbus.ModbusException;
import es.amplia.oda.core.commons.modbus.Register;
import es.amplia.oda.hardware.modbus.ModbusCounters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ModbusMasterAdapterTest {

    private static final int TEST_UNIT_ID = 5;
    private static final int TEST_REF = 10;
    private static final int TEST_COUNT = 4;
    private static final Boolean[] TEST_VALUES = new Boolean[] { true, false, true, false };
    private static final BitVector TEST_BIT_VECTOR = new BitVector(1);
    private static final InputRegister[] TEST_INPUT_REGISTERS = new InputRegister[4];
    private static final com.ghgande.j2mod.modbus.procimg.Register TEST_J2MOD_REGISTER = new SimpleRegister(5);
    private static final com.ghgande.j2mod.modbus.procimg.Register[] TEST_J2MOD_REGISTERS =
            new com.ghgande.j2mod.modbus.procimg.Register[4];
    private static final Register TEST_REGISTER = new Register(5);
    private static final Register[] TEST_REGISTERS = new Register[4];

    private final String TEST_DEVICE_ID = "testDeviceId";
    private final String TEST_DEVICE_MANUFACTURER = "testBitronic";

    @Mock
    private ModbusTCPMaster mockedModbusMaster;
    @Mock
    private ModbusTypeMapper mockedMapper;

    private ModbusMasterAdapter<ModbusTCPMaster> testModbusMasterAdapter;

    private MockedStatic<ModbusCounters> mockedModbusCounters;


    @BeforeEach
    public void setUp() throws Exception {

        // ini class to test
        testModbusMasterAdapter = new ModbusMasterAdapter<>(mockedModbusMaster, mockedMapper, TEST_DEVICE_ID, TEST_DEVICE_MANUFACTURER);

        mockedModbusCounters = mockStatic(ModbusCounters.class);
    }

    @AfterEach
    public void tearDown() {
        mockedModbusCounters.close();
    }

    @Test
    public void testConnect() throws Exception {
        testModbusMasterAdapter.connect();

        verify(mockedModbusMaster).connect();
    }

    @Test
    public void testConnectThrowsException() throws Exception {
        doThrow(new RuntimeException()).when(mockedModbusMaster).connect();

        assertThrows(ModbusException.class, () -> testModbusMasterAdapter.connect());
    }

    @Test
    public void testReadInputDiscrete() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readInputDiscretes(anyInt(), anyInt(), anyInt())).thenReturn(TEST_BIT_VECTOR);
        when(mockedMapper.mapBitVectorValues(any(BitVector.class))).thenReturn(new Boolean[] { true });

        boolean result = testModbusMasterAdapter.readInputDiscrete(TEST_UNIT_ID, TEST_REF);

        assertTrue(result);
        verify(mockedMapper).mapBitVectorValues(eq(TEST_BIT_VECTOR));
        verify(mockedModbusMaster).readInputDiscretes(eq(TEST_UNIT_ID), eq(TEST_REF), eq(1));
    }

    @Test
    public void testReadInputDiscretes() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readInputDiscretes(anyInt(), anyInt(), anyInt())).thenReturn(TEST_BIT_VECTOR);
        when(mockedMapper.mapBitVectorValues(any(BitVector.class))).thenReturn(TEST_VALUES);

        Boolean[] result = testModbusMasterAdapter.readInputDiscretes(TEST_UNIT_ID, TEST_REF, TEST_COUNT);

        assertArrayEquals(TEST_VALUES, result);
        verify(mockedMapper).mapBitVectorValues(eq(TEST_BIT_VECTOR));
        verify(mockedModbusMaster).readInputDiscretes(eq(TEST_UNIT_ID), eq(TEST_REF), eq(TEST_COUNT));
    }

    @Test
    public void testReadInputDiscretesThrowModbusException() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readInputDiscretes(anyInt(), anyInt(), anyInt()))
                .thenThrow(new com.ghgande.j2mod.modbus.ModbusException());

        assertThrows(ModbusException.class, () -> testModbusMasterAdapter.readInputDiscretes(TEST_UNIT_ID, TEST_REF, TEST_COUNT));
    }

    @Test
    public void testReadCoil() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readCoils(anyInt(), anyInt(), anyInt())).thenReturn(TEST_BIT_VECTOR);
        when(mockedMapper.mapBitVectorValues(any(BitVector.class))).thenReturn(new Boolean[] { true });

        boolean result = testModbusMasterAdapter.readCoil(TEST_UNIT_ID, TEST_REF);

        assertTrue(result);
        verify(mockedMapper).mapBitVectorValues(eq(TEST_BIT_VECTOR));
        verify(mockedModbusMaster).readCoils(eq(TEST_UNIT_ID), eq(TEST_REF), eq(1));
    }

    @Test
    public void testReadCoils() throws com.ghgande.j2mod.modbus.ModbusException {
        Boolean[] values = new Boolean[] { true, false, true, false };

        when(mockedModbusMaster.readCoils(anyInt(), anyInt(), anyInt())).thenReturn(TEST_BIT_VECTOR);
        when(mockedMapper.mapBitVectorValues(any(BitVector.class))).thenReturn(values);

        Boolean[] result = testModbusMasterAdapter.readCoils(TEST_UNIT_ID, TEST_REF, TEST_COUNT);

        assertArrayEquals(values, result);
        verify(mockedMapper).mapBitVectorValues(eq(TEST_BIT_VECTOR));
        verify(mockedModbusMaster).readCoils(eq(TEST_UNIT_ID), eq(TEST_REF), eq(TEST_COUNT));
    }

    @Test
    public void testReadInputCoilsThrowModbusException() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readCoils(anyInt(), anyInt(), anyInt()))
                .thenThrow(new com.ghgande.j2mod.modbus.ModbusException());

        assertThrows(ModbusException.class, () -> testModbusMasterAdapter.readCoils(TEST_UNIT_ID, TEST_REF, TEST_COUNT));
    }

    @Test
    public void testWriteCoil() throws com.ghgande.j2mod.modbus.ModbusException {
        testModbusMasterAdapter.writeCoil(TEST_UNIT_ID, TEST_REF, true);

        verify(mockedModbusMaster).writeCoil(eq(TEST_UNIT_ID), eq(TEST_REF), eq(true));
    }

    @Test
    public void testWriteCoilThrowsModbusException() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.writeCoil(anyInt(), anyInt(), anyBoolean()))
                .thenThrow(new com.ghgande.j2mod.modbus.ModbusException());

        assertThrows(ModbusException.class, () -> testModbusMasterAdapter.writeCoil(TEST_UNIT_ID, TEST_REF, true));
    }

    @Test
    public void testWriteCoils() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedMapper.mapValuesToBitVector(any(Boolean[].class))).thenReturn(TEST_BIT_VECTOR);

        testModbusMasterAdapter.writeCoils(TEST_UNIT_ID, TEST_REF, TEST_VALUES);

        verify(mockedMapper).mapValuesToBitVector(aryEq(TEST_VALUES));
        verify(mockedModbusMaster).writeMultipleCoils(eq(TEST_UNIT_ID), eq(TEST_REF), eq(TEST_BIT_VECTOR));
    }

    @Test
    public void testWriteCoilsThrowsModbusException() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedMapper.mapValuesToBitVector(any(Boolean[].class))).thenReturn(TEST_BIT_VECTOR);
        doThrow(new com.ghgande.j2mod.modbus.ModbusException()).when(mockedModbusMaster)
                .writeMultipleCoils(anyInt(), anyInt(), any(BitVector.class));

        assertThrows(ModbusException.class, () -> testModbusMasterAdapter.writeCoils(TEST_UNIT_ID, TEST_REF, TEST_VALUES));
    }

    @Test
    public void testReadInputRegister() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readInputRegisters(anyInt(), anyInt(), anyInt())).thenReturn(TEST_INPUT_REGISTERS);
        when(mockedMapper.mapInputRegisters(any(InputRegister[].class))).thenReturn(new Register[] { TEST_REGISTER });

        Register result = testModbusMasterAdapter.readInputRegister(TEST_UNIT_ID, TEST_REF);

        assertEquals(TEST_REGISTER, result);
        verify(mockedMapper).mapInputRegisters(eq(TEST_INPUT_REGISTERS));
        verify(mockedModbusMaster).readInputRegisters(eq(TEST_UNIT_ID), eq(TEST_REF), eq(1));
    }

    @Test
    public void testReadInputRegisters() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readInputRegisters(anyInt(), anyInt(), anyInt())).thenReturn(TEST_INPUT_REGISTERS);
        when(mockedMapper.mapInputRegisters(any(InputRegister[].class))).thenReturn(TEST_REGISTERS);

        Register[] result = testModbusMasterAdapter.readInputRegisters(TEST_UNIT_ID, TEST_REF, TEST_COUNT);

        assertArrayEquals(TEST_REGISTERS, result);
        verify(mockedMapper).mapInputRegisters(eq(TEST_INPUT_REGISTERS));
        verify(mockedModbusMaster).readInputRegisters(eq(TEST_UNIT_ID), eq(TEST_REF), eq(TEST_COUNT));
    }

    @Test
    public void testReadInputRegistersThrowsModbusException() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readInputRegisters(anyInt(), anyInt(), anyInt()))
                .thenThrow(new com.ghgande.j2mod.modbus.ModbusException());

        assertThrows(ModbusException.class, () -> testModbusMasterAdapter.readInputRegister(TEST_UNIT_ID, TEST_REF));
    }

    @Test
    public void testReadHoldingRegister() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readMultipleRegisters(anyInt(), anyInt(), anyInt())).thenReturn(TEST_J2MOD_REGISTERS);
        when(mockedMapper.mapInputRegisters(any(InputRegister[].class))).thenReturn(new Register[] { TEST_REGISTER });

        Register result = testModbusMasterAdapter.readHoldingRegister(TEST_UNIT_ID, TEST_REF);

        assertEquals(TEST_REGISTER, result);
        verify(mockedMapper).mapInputRegisters(eq(TEST_J2MOD_REGISTERS));
        verify(mockedModbusMaster).readMultipleRegisters(eq(TEST_UNIT_ID), eq(TEST_REF), eq(1));
    }

    @Test
    public void testReadHoldingRegisters() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readMultipleRegisters(anyInt(), anyInt(), anyInt())).thenReturn(TEST_J2MOD_REGISTERS);
        when(mockedMapper.mapInputRegisters(any(com.ghgande.j2mod.modbus.procimg.Register[].class)))
                .thenReturn(TEST_REGISTERS);

        Register[] result = testModbusMasterAdapter.readHoldingRegisters(TEST_UNIT_ID, TEST_REF, TEST_COUNT);

        assertArrayEquals(TEST_REGISTERS, result);
        verify(mockedMapper).mapInputRegisters(eq(TEST_J2MOD_REGISTERS));
        verify(mockedModbusMaster).readMultipleRegisters(eq(TEST_UNIT_ID), eq(TEST_REF), eq(TEST_COUNT));
    }

    @Test
    public void testReadHoldingRegistersThrowsModbusException() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedModbusMaster.readMultipleRegisters(anyInt(), anyInt(), anyInt()))
                .thenThrow(new com.ghgande.j2mod.modbus.ModbusException());

        assertThrows(ModbusException.class, () -> testModbusMasterAdapter.readHoldingRegisters(TEST_UNIT_ID, TEST_REF, TEST_COUNT));
    }

    @Test
    public void testWriteHoldingRegister() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedMapper.mapToJ2ModbusRegister(any(Register.class))).thenReturn(TEST_J2MOD_REGISTER);

        testModbusMasterAdapter.writeHoldingRegister(TEST_UNIT_ID, TEST_REF, TEST_REGISTER);

        verify(mockedMapper).mapToJ2ModbusRegister(eq(TEST_REGISTER));
        verify(mockedModbusMaster).writeSingleRegister(eq(TEST_UNIT_ID), eq(TEST_REF), eq(TEST_J2MOD_REGISTER));
    }

    @Test
    public void testWriteHoldingRegisterThrowsModbusException() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedMapper.mapToJ2ModbusRegister(any(Register.class))).thenReturn(TEST_J2MOD_REGISTER);
        doThrow(new com.ghgande.j2mod.modbus.ModbusException()).when(mockedModbusMaster)
                .writeSingleRegister(anyInt(), anyInt(), any(com.ghgande.j2mod.modbus.procimg.Register.class));

        assertThrows(ModbusException.class, () -> testModbusMasterAdapter.writeHoldingRegister(TEST_UNIT_ID, TEST_REF, TEST_REGISTER));
    }

    @Test
    public void testWriteHoldingRegisters() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedMapper.mapToJ2ModbusRegisters(any(Register[].class))).thenReturn(TEST_J2MOD_REGISTERS);

        testModbusMasterAdapter.writeHoldingRegisters(TEST_UNIT_ID, TEST_REF, TEST_REGISTERS);

        verify(mockedMapper).mapToJ2ModbusRegisters(eq(TEST_REGISTERS));
        verify(mockedModbusMaster).writeMultipleRegisters(eq(TEST_UNIT_ID), eq(TEST_REF), eq(TEST_J2MOD_REGISTERS));
    }

    @Test
    public void testWriteHoldingRegistersThrowsModbusException() throws com.ghgande.j2mod.modbus.ModbusException {
        when(mockedMapper.mapToJ2ModbusRegisters(any(Register[].class))).thenReturn(TEST_J2MOD_REGISTERS);
        doThrow(new com.ghgande.j2mod.modbus.ModbusException()).when(mockedModbusMaster)
                .writeMultipleRegisters(anyInt(), anyInt(), any(com.ghgande.j2mod.modbus.procimg.Register[].class));

        assertThrows(ModbusException.class, () -> testModbusMasterAdapter.writeHoldingRegisters(TEST_UNIT_ID, TEST_REF, TEST_REGISTERS));
    }

    @Test
    public void testDisconnect() {
        testModbusMasterAdapter.disconnect();

        verify(mockedModbusMaster).disconnect();
    }

    @Test
    public void testGetDeviceId() throws Exception {
        String deviceId = testModbusMasterAdapter.getDeviceId();

        Assertions.assertEquals(TEST_DEVICE_ID, deviceId);
    }

    @Test
    public void testGetDeviceManufacturer() throws Exception {
        String deviceManufacturer = testModbusMasterAdapter.getDeviceManufacturer();

        Assertions.assertEquals(TEST_DEVICE_MANUFACTURER, deviceManufacturer);
    }
}