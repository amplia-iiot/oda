package es.amplia.oda.core.commons.modbus;

public class ModbusException extends RuntimeException {

    public ModbusException() {}

    public ModbusException(String message) {
        super(message);
    }

    public ModbusException(String message, Throwable cause) {
        super(message, cause);
    }
}
