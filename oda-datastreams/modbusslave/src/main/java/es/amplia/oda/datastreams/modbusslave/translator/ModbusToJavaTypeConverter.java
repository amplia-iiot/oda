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

    public static String DATA_TYPE_SHORT = "Short";
    public static String DATA_TYPE_INT = "Int";
    public static String DATA_TYPE_FLOAT = "Float";
    public static String DATA_TYPE_DOUBLE = "Double";
    public static String DATA_TYPE_LONG = "Long";
    public static String DATA_TYPE_LIST = "List";


    public static Object convertRegister(byte[] modbusRegisters, String dataTypeToConvert) {
        if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_SHORT)) {
            return convertRegisterToShort(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_INT)) {
            return convertRegisterToInt(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_FLOAT)) {
            return convertRegistersToFloat(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_DOUBLE)) {
            return convertRegistersToDouble(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_LONG)) {
            return convertRegistersToLong(modbusRegisters);
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_LIST)) {
            return convertRegistersToByteArray(modbusRegisters);
        } else {
            log.error("Datatype " + dataTypeToConvert + " not supported");
            return null;
        }
    }

    public static int getNumRegisters(String dataTypeToConvert, int numRegisterInBlock) {
        if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_SHORT)) {
            return ONE_REGISTER;
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_INT)) {
            return TWO_REGISTERS;
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_FLOAT)) {
            return TWO_REGISTERS;
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_DOUBLE)) {
            return FOUR_REGISTERS;
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_LONG)) {
            return FOUR_REGISTERS;
        } else if (dataTypeToConvert.equalsIgnoreCase(DATA_TYPE_LIST)) {
                return numRegisterInBlock;
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
