package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.Register;

import java.nio.ByteBuffer;

class JavaTypeToModbusTypeConverter {

    private static final int BYTES_PER_REGISTER = 2;
    static final int TWO_REGISTERS = 2;
    static final int FOUR_REGISTERS = 4;
    private static final int FIRST_BYTE = 0;
    private static final int SECOND_BYTE = 1;
    private static final int THIRD_BYTE = 2;
    private static final int FOURTH_BYTE = 3;
    private static final int FIFTH_BYTE = 4;
    private static final int SIXTH_BYTE = 5;
    private static final int SEVENTH_BYTE = 6;
    private static final int EIGHTH_BYTE = 7;


    Register convertByteArrayToRegister(byte[] value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER).put(value).array();
        return new Register(bytes[FIRST_BYTE], bytes[SECOND_BYTE]);
    }

    Register convertShortToRegister(short value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER).putShort(value).array();
        return new Register(bytes[FIRST_BYTE], bytes[SECOND_BYTE]);
    }

    Register[] convertIntegerToRegisters(int value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER * TWO_REGISTERS).putInt(value).array();
        return new Register[] {
                new Register(bytes[FIRST_BYTE], bytes[SECOND_BYTE]),
                new Register(bytes[THIRD_BYTE], bytes[FOURTH_BYTE])
        };
    }

    Register[] convertFloatToRegisters(float value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER * TWO_REGISTERS).putFloat(value).array();
        return new Register[] {
                new Register(bytes[FIRST_BYTE], bytes[SECOND_BYTE]),
                new Register(bytes[THIRD_BYTE], bytes[FOURTH_BYTE])
        };
    }

    Register[] convertLongToRegisters(long value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER * FOUR_REGISTERS).putLong(value).array();
        return new Register[] {
                new Register(bytes[FIRST_BYTE], bytes[SECOND_BYTE]),
                new Register(bytes[THIRD_BYTE], bytes[FOURTH_BYTE]),
                new Register(bytes[FIFTH_BYTE], bytes[SIXTH_BYTE]),
                new Register(bytes[SEVENTH_BYTE], bytes[EIGHTH_BYTE])
        };
    }

    Register[] convertDoubleToRegisters(double value) {
        byte[] bytes = ByteBuffer.allocate(BYTES_PER_REGISTER * FOUR_REGISTERS).putDouble(value).array();
        return new Register[] {
                new Register(bytes[FIRST_BYTE], bytes[SECOND_BYTE]),
                new Register(bytes[THIRD_BYTE], bytes[FOURTH_BYTE]),
                new Register(bytes[FIFTH_BYTE], bytes[SIXTH_BYTE]),
                new Register(bytes[SEVENTH_BYTE], bytes[EIGHTH_BYTE])
        };
    }
}
