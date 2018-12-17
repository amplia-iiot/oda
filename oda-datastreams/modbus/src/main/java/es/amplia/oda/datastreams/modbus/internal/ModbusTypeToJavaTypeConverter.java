package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.Register;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class ModbusTypeToJavaTypeConverter {

    private static final int BYTES_PER_REGISTER = 2;
    private static final int TWO_REGISTERS = 2;
    private static final int FOUR_REGISTERS = 4;

    byte[] convertRegisterToByteArray(Register register) {
        return register.toBytes();
    }

    short convertRegisterToShort(Register register) {
        return register.toShort();
    }

    int convertRegistersToInteger(Register[] registers) {
        checkTwoRegistersParam(registers);
        byte[] bytes = getBytes(registers);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private void checkTwoRegistersParam(Register[] registers) {
        if (registers.length != TWO_REGISTERS) {
            throw new IllegalArgumentException("Invalid register array to convert to 32 bits return type");
        }
    }

    private byte[] getBytes(Register[] registers) {
        byte[] bytes = new byte[registers.length * BYTES_PER_REGISTER];
        for (int i = 0; i < registers.length; i++) {
            bytes[i * BYTES_PER_REGISTER] = registers[i].toBytes()[0];
            bytes[i * BYTES_PER_REGISTER + 1] = registers[i].toBytes()[1];
        }
        return bytes;
    }

    float convertRegistersToFloat(Register[] registers) {
        checkTwoRegistersParam(registers);
        byte[] bytes = getBytes(registers);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    long convertRegistersToLong(Register[] registers) {
        checkFourRegistersParam(registers);
        byte[] bytes = getBytes(registers);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    private void checkFourRegistersParam(Register[] registers) {
        if (registers.length != FOUR_REGISTERS) {
            throw new IllegalArgumentException("Invalid register array to convert to 64 bits return type");
        }
    }

    double convertRegistersToDouble(Register[] registers) {
        checkFourRegistersParam(registers);
        byte[] bytes = getBytes(registers);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
    }
}
