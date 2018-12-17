package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.Register;

import java.nio.ByteBuffer;

class JavaTypeToModbusTypeConverter {

    private static final int BYTES_PER_REGISTER = 2;
    static final int TWO_REGISTERS = 2;
    static final int FOUR_REGISTERS = 4;


    Register convertByteArrayToRegister(byte[] value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER).put(value).array();
        return new Register(bytes[0], bytes[1]);
    }

    Register convertShortToRegister(short value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER).putShort(value).array();
        return new Register(bytes[0], bytes[1]);
    }

    Register[] convertIntegerToRegisters(int value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER * TWO_REGISTERS).putInt(value).array();
        return new Register[] {
                new Register(bytes[0], bytes[1]),
                new Register(bytes[2], bytes[3])
        };
    }

    Register[] convertFloatToRegisters(float value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER * TWO_REGISTERS).putFloat(value).array();
        return new Register[] {
                new Register(bytes[0], bytes[1]),
                new Register(bytes[2], bytes[3])
        };
    }

    Register[] convertLongToRegisters(long value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER * FOUR_REGISTERS).putLong(value).array();
        return new Register[] {
                new Register(bytes[0], bytes[1]),
                new Register(bytes[2], bytes[3]),
                new Register(bytes[4], bytes[5]),
                new Register(bytes[6], bytes[7])
        };
    }

    Register[] convertDoubleToRegisters(double value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER * FOUR_REGISTERS).putDouble(value).array();
        return new Register[] {
                new Register(bytes[0], bytes[1]),
                new Register(bytes[2], bytes[3]),
                new Register(bytes[4], bytes[5]),
                new Register(bytes[6], bytes[7])
        };
    }
}
