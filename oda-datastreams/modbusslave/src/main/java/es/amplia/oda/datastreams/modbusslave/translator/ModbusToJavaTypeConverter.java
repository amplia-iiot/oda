package es.amplia.oda.datastreams.modbusslave.translator;


import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
class ModbusToJavaTypeConverter {

    public static final int BYTES_PER_REGISTER = 2;
    static final int ONE_REGISTER = 1;
    static final int TWO_REGISTERS = 2;
    static final int FOUR_REGISTERS = 4;


    public static Object convertRegister(byte[] modbusRegisters, String dataTypeToConvert) {
        if (dataTypeToConvert.equalsIgnoreCase("Short")) {
            return convertRegisterToShort(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase("Int")) {
            return convertRegisterToInt(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase("Float")) {
            return convertRegistersToFloat(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase("Double")) {
            return convertRegistersToDouble(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase("Long")) {
            return convertRegistersToLong(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase("List")) {
            return convertRegistersToByteArray(modbusRegisters);
        } else {
            log.error("Datatype " + dataTypeToConvert + " not supported");
            return null;
        }
    }

    public static int getNumRegisters(String dataTypeToConvert, Integer numRegistersToGet, int numRegisterInBlock) {
        if (dataTypeToConvert.equalsIgnoreCase("Short")) {
            return ONE_REGISTER;
        } else if (dataTypeToConvert.equalsIgnoreCase("Int")) {
            return TWO_REGISTERS;
        } else if (dataTypeToConvert.equalsIgnoreCase("Float")) {
            return TWO_REGISTERS;
        } else if (dataTypeToConvert.equalsIgnoreCase("Double")) {
            return FOUR_REGISTERS;
        } else if (dataTypeToConvert.equalsIgnoreCase("Long")) {
            return FOUR_REGISTERS;
        } else if (dataTypeToConvert.equalsIgnoreCase("List")) {
            if (numRegistersToGet == null || numRegistersToGet > numRegisterInBlock) {
                return numRegisterInBlock;
            } else {
                return numRegistersToGet;
            }
        } else {
            log.error("Datatype " + dataTypeToConvert + " not supported");
            return 0;
        }
    }

    private static Short convertRegisterToShort(byte[] value) {
        if (value.length != ONE_REGISTER * BYTES_PER_REGISTER) {
            log.error("Wrong length of bytes for short value");
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN);
        return buffer.getShort();
    }

    private static Integer convertRegisterToInt(byte[] value){
        if (value.length != TWO_REGISTERS * BYTES_PER_REGISTER) {
            log.error("Wrong length of bytes for int value");
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    private static Float convertRegistersToFloat(byte[] value) {
        if (value.length != TWO_REGISTERS * BYTES_PER_REGISTER) {
            log.error("Wrong length of bytes for float value");
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN);
        return buffer.getFloat();
    }

    private static Long convertRegistersToLong(byte[] value)  {
        if (value.length != FOUR_REGISTERS * BYTES_PER_REGISTER) {
            log.error("Wrong length of bytes for long value");
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN);
        return buffer.getLong();
    }

    private static Double convertRegistersToDouble(byte[] value) {
        if (value.length != FOUR_REGISTERS * BYTES_PER_REGISTER) {
            log.error("Wrong length of bytes for double value");
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN);
        return buffer.getDouble();
    }

    private static byte[] convertRegistersToByteArray(byte[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN);
        return buffer.array();
    }
}
