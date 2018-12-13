package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.core.commons.modbus.Register;

import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.util.Arrays;

class ModbusTypeMapper {

    Boolean[] mapBitVectorValues(BitVector bitVector) {
        Boolean[] result = new Boolean[bitVector.size()];
        for (int i = 0; i < bitVector.size(); i++) {
            result[i] = bitVector.getBit(i);
        }
        return result;
    }

    BitVector mapValuesToBitVector(Boolean[] values) {
        BitVector bitVector = new BitVector(values.length);
        for (int i = 0; i < values.length; i++) {
            bitVector.setBit(i, values[i]);
        }
        return bitVector;
    }

    Register[] mapInputRegisters(InputRegister[] inputRegisters) {
        return Arrays.stream(inputRegisters).map(inputRegister -> new Register(inputRegister.getValue()))
                .toArray(Register[]::new);
    }

    com.ghgande.j2mod.modbus.procimg.Register mapToJ2ModbusRegister(Register register) {
        return new SimpleRegister(register.getValue());
    }

    com.ghgande.j2mod.modbus.procimg.Register[] mapToJ2ModbusRegisters(Register[] registers) {
        return Arrays.stream(registers).map(register -> new SimpleRegister(register.getValue()))
                .toArray(com.ghgande.j2mod.modbus.procimg.Register[]::new);
    }
}
