package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.core.commons.modbus.Register;

import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModbusTypeMapperTest {

    private final ModbusTypeMapper testModbusTypeMapper = new ModbusTypeMapper();

    @Test
    public void testMapBitVectorValues() {
        BitVector bitVector = new BitVector(4);
        bitVector.setBit(0, true);
        bitVector.setBit(1, true);
        bitVector.setBit(2, false);
        bitVector.setBit(3, false);

        Boolean[] result = testModbusTypeMapper.mapBitVectorValues(bitVector);

        assertEquals(true, result[0]);
        assertEquals(true, result[1]);
        assertEquals(false, result[2]);
        assertEquals(false, result[3]);
    }

    @Test
    public void testMapValuesToBitVector() {
        Boolean[] values = new Boolean[] { true, true, false, false };

        BitVector result = testModbusTypeMapper.mapValuesToBitVector(values);

        assertTrue(result.getBit(0));
        assertTrue(result.getBit(1));
        assertFalse(result.getBit(2));
        assertFalse(result.getBit(3));
    }

    @Test
    public void testMapInputRegister() {
        InputRegister[] values = new InputRegister[4];
        values[0] = new SimpleRegister(1);
        values[1] = new SimpleRegister(2);
        values[2] = new SimpleRegister(3);
        values[3] = new SimpleRegister(4);

        Register[] result = testModbusTypeMapper.mapInputRegisters(values);

        assertEquals(1, result[0].getValue());
        assertEquals(2, result[1].getValue());
        assertEquals(3, result[2].getValue());
        assertEquals(4, result[3].getValue());
    }

    @Test
    public void testMapToJ2ModbusRegister() {
        Register register = new Register(5);

        com.ghgande.j2mod.modbus.procimg.Register result = testModbusTypeMapper.mapToJ2ModbusRegister(register);

        assertEquals(5, result.getValue());
    }

    @Test
    public void testMapToJ2ModbusRegisters() {
        Register[] registers = new Register[4];
        registers[0] = new Register(1);
        registers[1] = new Register(2);
        registers[2] = new Register(3);
        registers[3] = new Register(4);

        com.ghgande.j2mod.modbus.procimg.Register[] result = testModbusTypeMapper.mapToJ2ModbusRegisters(registers);

        assertEquals(1, result[0].getValue());
        assertEquals(2, result[1].getValue());
        assertEquals(3, result[2].getValue());
        assertEquals(4, result[3].getValue());
    }
}